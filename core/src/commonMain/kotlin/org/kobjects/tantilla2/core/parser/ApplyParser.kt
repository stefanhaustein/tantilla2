package org.kobjects.tantilla2.core.parser

import org.kobjects.tantilla2.core.classifier.InstantiableMetaType
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.LambdaScope
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.node.expression.Apply
import org.kobjects.tantilla2.core.node.expression.LambdaReference
import org.kobjects.tantilla2.core.node.expression.ListLiteral
import org.kobjects.tantilla2.core.node.expression.RawNode
import org.kobjects.tantilla2.core.type.NoneType

object ApplyParser {

    fun parseTrailingClosure(
        tokenizer: TantillaScanner,
        context: ParsingContext,
        expectedType: FunctionType,
    ): Node {
        val parameterNames: List<String>
        if (tokenizer.current.text == "(") {
            val type = TypeParser.parseFunctionType(tokenizer, context, isMethod = false)
            if (!expectedType.isAssignableFrom(type)) {
                throw tokenizer.exception("Function type $type does not match expected type $expectedType")
            }
            parameterNames = type.parameters.map { it.name }
            tokenizer.consume(":")
        } else {
            parameterNames = List(expectedType.parameters.size) { "${'$'}$it" }
        }

        val lamnbdaScope = LambdaScope(context.scope) // resolvedType = type)
        for (i in expectedType.parameters.indices) {
            val parameter = expectedType.parameters[i]
            lamnbdaScope.declareLocalVariable(parameterNames[i], parameter.type, false)
        }

        val body = Parser.parseDefinitionsAndStatements(tokenizer, context.depth + 1, lamnbdaScope, definitionScope = lamnbdaScope)

        return LambdaReference(expectedType, lamnbdaScope.locals.size, body, implicit = false)
    }

    fun parseMaybeApply(
        tokenizer: TantillaScanner,
        context: ParsingContext,
        operation: Node,
        self: Node?,
        openingParenConsumed: Boolean,
        asMethod: Boolean,
    ): Node {
        if (!openingParenConsumed && tokenizer.tryConsume("@")) {
            require(!asMethod) { "Can't combine @ with methods." }
            return RawNode(operation)
        }

        val type = operation.returnType

        if (type !is FunctionType) {
            // Not a function, just skip () and error otherwise

            if (openingParenConsumed || tokenizer.tryConsume("(")) {
                tokenizer.consume(")") { "Empty parameter list expected." }
            }
            return operation
        }

        // Don't imply constructor calls.
        val parentesizedArgsList = openingParenConsumed || tokenizer.tryConsume("(")
        if (!parentesizedArgsList && type is InstantiableMetaType) {
            return operation
        }

        val expectedParameters = type.parameters
        val parameterExpressions = MutableList<Node?>(expectedParameters.size) { null }
        val parameterSerialization = mutableListOf<Apply.ParameterSerialization>()
        var index = 0
        if (self != null) {
            parameterExpressions[index++] = self
            if (!asMethod) {
                parameterSerialization.add(Apply.ParameterSerialization("", self))
            }
        }

        val indexMap = mutableMapOf<String, Int>()
        for (i in expectedParameters.indices) {
            indexMap[expectedParameters[i].name] = i
        }

        val varargs = mutableListOf<Node>()
        var varargIndex = -1
        var nameRequired = false

        val parseParameterList = if (parentesizedArgsList) !tokenizer.tryConsume(")")
        else ((type.returnType == NoneType || type.hasRequiredParameters())
                && tokenizer.current.type != TokenType.EOF
                && tokenizer.current.type != TokenType.LINE_BREAK
                && tokenizer.current.text != ":"
                && !asMethod
                && self == null)

        if (parseParameterList)  {
            do {
                var name = ""
                if (tokenizer.current.type == TokenType.IDENTIFIER && tokenizer.lookAhead(1).text == "=") {
                    name = tokenizer.consume(TokenType.IDENTIFIER).text
                    tokenizer.consume("=")
                    nameRequired = true
                    index = indexMap[name] ?: throw tokenizer.exception("Parameter name '$name' not found.")
                } else if (nameRequired) {
                    throw tokenizer.exception("Named parameter required here.")
                } else if (index >= expectedParameters.size) {
                    throw tokenizer.exception("Expected parameters $expectedParameters exceeded; index: $index")
                }
                val expectedParameter = expectedParameters[index]
                val expression =
                    TantillaExpressionParser.parseExpression(
                        tokenizer,
                        context,
                        expectedParameter.type
                    )

                parameterSerialization.add(Apply.ParameterSerialization(name, expression))
                if (expectedParameter.isVararg) {
                    varargs.add(expression)
                    varargIndex = index
                } else {
                    parameterExpressions[index++] = expression
                }
            } while (tokenizer.tryConsume(","))

            if (parentesizedArgsList) {
                tokenizer.consume(")")
            }
        }

        if (varargIndex != -1) {
            parameterExpressions[varargIndex] = ListLiteral(varargs)
        }

        var missingFunctionParameter = false
        for (i in expectedParameters.indices) {
            if (parameterExpressions[i] == null && expectedParameters[i].type is FunctionType) {
                missingFunctionParameter = true
                break
            }
        }

        if (missingFunctionParameter && tokenizer.tryConsume(":")) {
            var found = false
            for (i in expectedParameters.indices) {
                if (parameterExpressions[i] == null && expectedParameters[i].type is FunctionType) {
                    found = true
                    parameterExpressions[i] = parseTrailingClosure(tokenizer, context, expectedParameters[i].type as FunctionType)
                    break
                }
            }
            if (!found) {
                throw tokenizer.exception("No missing lambda parameter found fo assign trailing closure to.")
            }
        }

        for (i in expectedParameters.indices) {
            val expectedParameter = expectedParameters[i]
            if (parameterExpressions[i] == null) {
                if (expectedParameter.defaultValueExpression == null) {
                    if (expectedParameter.isVararg) {
                        parameterExpressions[i] = ListLiteral(varargs)
                    } else {
                        throw tokenizer.exception("Parameter '${expectedParameter.name}' is missing.")
                    }
                } else {
                    parameterExpressions[i] = expectedParameter.defaultValueExpression
                }
            }
        }

        return Apply(
            operation,
            List(parameterExpressions.size) { parameterExpressions[it]!!},
            parameterSerialization.toList(),
            parentesizedArgsList,
            asMethod
        )
    }

}