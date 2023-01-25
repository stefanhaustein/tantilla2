package org.kobjects.tantilla2.core.parser

import org.kobjects.tantilla2.core.classifier.InstantiableMetaType
import org.kobjects.tantilla2.core.collection.PairType
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.LambdaScope
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.node.expression.*
import org.kobjects.tantilla2.core.type.NoneType
import org.kobjects.tantilla2.core.type.Type

object ApplyParser {

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


        var missingFunctionParameter = mutableMapOf<String, Int>()
        for (i in expectedParameters.indices) {
            if ((parameterExpressions[i] == null || i == varargIndex) && isFunctionOrFunctionPairType(expectedParameters[i].type)) {
                missingFunctionParameter[expectedParameters[i].name] = i
            }
        }

        while (missingFunctionParameter.isNotEmpty()) {
            if (tokenizer.tryConsume(":")) {
                for (i in expectedParameters.indices) {
                    // The null check excludes varargs, the type check excludes lambda pairs
                    if (parameterExpressions[i] == null && expectedParameters[i].type is FunctionType) {
                        val node = parseTrailingClosure(
                            tokenizer,
                            context,
                            expectedParameters[i].type as FunctionType
                        )
                        parameterExpressions[i] = node
                        missingFunctionParameter.remove(expectedParameters[i].name)
                        parameterSerialization.add(Apply.ParameterSerialization("", node, true))
                        break
                    }
                }
            } else {
                val name = tryConsumeNamedLambdaPrefix(tokenizer, missingFunctionParameter.keys) ?: break
                val index = missingFunctionParameter[name]!!
                val type = expectedParameters[index].type
                val expression = if (type is PairType) {
                    val exprA = TantillaExpressionParser.parseExpression(tokenizer, context, type.typeA)
                    tokenizer.consume(":")
                    val exprB = parseTrailingClosure(tokenizer, context, type.typeB as FunctionType)
                    PairNode(exprA, exprB)
                } else {
                    tokenizer.consume(":") { "Colon expected after trailing closure parameter name." }
                    parseTrailingClosure(
                        tokenizer,
                        context,
                        expectedParameters[index].type as FunctionType
                    )
                }
                parameterSerialization.add(Apply.ParameterSerialization(name, expression, true))
                if (expectedParameters[index].isVararg) {
                    varargIndex = index
                    varargs.add(expression)
                } else {
                    parameterExpressions[index] = expression
                    missingFunctionParameter.remove(name)
                }
            }
        }

        if (varargIndex != -1) {
            parameterExpressions[varargIndex] = ListLiteral(varargs)
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

    fun isFunctionOrFunctionPairType(type: Type) =
        type is FunctionType
        || (type is PairType &&
                type.typeA is FunctionType && type.typeB is FunctionType)


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

        val lambdaScope = LambdaScope(context.scope) // resolvedType = type)
        for (i in expectedType.parameters.indices) {
            val parameter = expectedType.parameters[i]
            lambdaScope.declareLocalVariable(parameterNames[i], parameter.type, false)
        }

        val body = Parser.parseDefinitionsAndStatements(tokenizer, context.depth + 1, lambdaScope, definitionScope = lambdaScope)

        return LambdaReference(expectedType, lambdaScope.locals.size, body, implicit = true)
    }

    fun tryConsumeNamedLambdaPrefix(tokenizer: TantillaScanner, names: Set<String>): String? {
        var i = 0
        while (tokenizer.lookAhead(i).type == TokenType.COMMENT || tokenizer.lookAhead(i).type == TokenType.LINE_BREAK) {
            i++
        }
        val name = tokenizer.lookAhead(i).text
        if (name in names) {
            for (j in 0 .. i) {
                tokenizer.consume()
            }
            return name
        }
        return null
    }




}