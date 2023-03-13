package org.kobjects.tantilla2.core.parser

import org.kobjects.tantilla2.core.classifier.InstantiableMetaType
import org.kobjects.tantilla2.core.collection.PairType
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.node.expression.*
import org.kobjects.tantilla2.core.parser.Parser.indent
import org.kobjects.tantilla2.core.type.GenericTypeMap
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
        val genericTypeMap = GenericTypeMap()

        if (!openingParenConsumed && tokenizer.tryConsume("@")) {
            require(!asMethod) { "Can't combine @ with methods." }
            return RawNode(operation)
        }

        val type = operation.returnType.mapTypeParametersToTypeVariables(genericTypeMap)

        println("ParseMaybeApply - Type: $type")

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
                val expression = TantillaExpressionParser.parseExpression(
                        tokenizer,
                        context,
                        expectedParameter.type,
                        genericTypeMap)

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
            if (tokenizer.current.text == ":" || tokenizer.current.text == "::") {
                for (i in expectedParameters.indices) {
                    // The null check excludes varargs, the type check excludes lambda pairs
                    if (parameterExpressions[i] == null && expectedParameters[i].type is FunctionType) {
                        println("parseMaybyApply -- expectedParameters[$i].type: ${expectedParameters[i].type}")
                        val node = LambdaParser.parseTrailingClosure(
                            tokenizer,
                            context,
                            expectedParameters[i].type as FunctionType,
                            genericTypeMap
                        )
                        parameterExpressions[i] = node
                        missingFunctionParameter.remove(expectedParameters[i].name)
                        parameterSerialization.add(Apply.ParameterSerialization("", node, true))
                        break
                    }
                }
            } else {
                val name = tryConsumeNamedLambdaPrefix(tokenizer, context.depth, missingFunctionParameter.keys) ?: break
                val index = missingFunctionParameter[name]!!
                val type = expectedParameters[index].type
                val expression = if (type.unparameterized() is PairType) {
                    val exprA = TantillaExpressionParser.parseExpression(tokenizer, context, type.genericParameterTypes[0])
                    val exprB = LambdaParser.parseTrailingClosure(tokenizer, context, type.genericParameterTypes[1] as FunctionType, genericTypeMap)
                    PairNode(exprA, exprB)
                } else {
                    LambdaParser.parseTrailingClosure(
                        tokenizer,
                        context,
                        expectedParameters[index].type as FunctionType,
                        genericTypeMap
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
            type.returnType.resolveGenerics(null, genericTypeMap),
            List(parameterExpressions.size) { parameterExpressions[it]!!},
            parameterSerialization.toList(),
            parentesizedArgsList,
            asMethod
        )
    }

    fun isFunctionOrFunctionPairType(type: Type) =
        type is FunctionType
        || (type.unparameterized() is PairType
                && type.genericParameterTypes[0].unparameterized() is FunctionType
                && type.genericParameterTypes[1].unparameterized() is FunctionType)


    fun tryConsumeNamedLambdaPrefix(tokenizer: TantillaScanner, indent: Int, names: Set<String>): String? {
        var i = 0
        while (true) {
            val token = tokenizer.lookAhead(i)
            val ok = token.type == TokenType.COMMENT
                    || (token.type == TokenType.LINE_BREAK
                    && token.text.indent() >= indent)
            if (!ok) {
                break
            }
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