package org.kobjects.tantilla2.core.parser

import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.LambdaScope
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.node.expression.FakeLambda
import org.kobjects.tantilla2.core.node.expression.LambdaReference
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.Type

object LambdaParser {

    fun parseLambdaParameterList(tokenizer: TantillaScanner, context: ParsingContext, expectedType: FunctionType? = null): Pair<FunctionType, List<String>> {
        val parameterNames: List<String>
        val type: FunctionType
        if (tokenizer.current.text == "(") {
            type = TypeParser.parseFunctionType(tokenizer, context, isMethod = false)
            if (expectedType != null && !expectedType.isAssignableFrom(type)) {
                throw tokenizer.exception("Function type $type does not match expected type $expectedType")
            }
            parameterNames = type.parameters.map { it.name }
        } else {
            if (expectedType == null) {
                throw tokenizer.exception("For lambdas with unknown type, a full parameter list starting with '(' is expected.")
            }
            type = expectedType
            val names = mutableListOf<String>()
            if (tokenizer.current.text != ":") {
                do {
                    names.add(tokenizer.consume(TokenType.IDENTIFIER).text)
                } while (tokenizer.tryConsume(","))
            }
            if (names.size > type.parameters.size) {
                throw tokenizer.exception("${names.size} parameters provided, but only ${type.parameters.size} parameters expected (type: $type).")
            }
            while (names.size < type.parameters.size) {
                names.add("$${names.size}")
            }
            parameterNames = names.toList()
        }
        return type to parameterNames
    }



    // Add support for known signature later
    fun parseLambda(
        tokenizer: TantillaScanner,
        context: ParsingContext,
        expectedType: FunctionType? = null
    ): Node {
        tokenizer.consume("lambda")
        return parseParameterizedClosure(tokenizer, context, expectedType, implicit = false)
    }

    fun parseParameterizedClosure(
            tokenizer: TantillaScanner,
            context: ParsingContext,
            expectedType: FunctionType? = null,
            implicit: Boolean,
            genericTypeMap: GenericTypeMap = GenericTypeMap()
    ): Node {
        val parsedParameters = parseLambdaParameterList(tokenizer, context, expectedType)
        val type = parsedParameters.first
        val parameterNames = parsedParameters.second

        println("*** Lambda type parsed: $type")

        tokenizer.consume(":")
        val lambdaScope = LambdaScope(context.scope) // resolvedType = type)
        for (i in type.parameters.indices) {
            val parameter = type.parameters[i]
            lambdaScope.declareLocalVariable(parameterNames[i], parameter.type, false)
        }

        return parseClosureBody(tokenizer, context, type, lambdaScope, expectedType?.returnType, implicit, genericTypeMap)
    }



    fun parseClosureBody(
        tokenizer: TantillaScanner,
        context: ParsingContext,
        functionType: FunctionType,
        lambdaScope: LambdaScope,
        expectedReturnType: Type?,
        implicit: Boolean,
        genericTypeMap: GenericTypeMap
    ): Node {
        val body = Parser.parseDefinitionsAndStatements(tokenizer, context.depth + 1, lambdaScope, definitionScope = lambdaScope)

        val refinedBody = TantillaExpressionParser.matchType(body, expectedReturnType, genericTypeMap)

        val refinedType = if (expectedReturnType == null) FunctionType.Impl(refinedBody.returnType, functionType.parameters)
          else functionType

        return LambdaReference(refinedType.resolveGenerics(null, genericTypeMap), lambdaScope.locals.size, refinedBody, implicit = implicit)
    }



    fun parseTrailingClosure(
        tokenizer: TantillaScanner,
        context: ParsingContext,
        expectedType: FunctionType,
        genericTypeMap: GenericTypeMap
    ): Node {
        if (tokenizer.tryConsume("::")) {
            return parseParameterizedClosure(tokenizer, context, expectedType, implicit = true, genericTypeMap)
        }

        tokenizer.consume(":") {"':' or '::' expected for trailing closure."}
        val parameterNames = List(expectedType.parameters.size) { "${'$'}$it" }
        val lambdaScope = LambdaScope(context.scope) // resolvedType = type)
        for (i in expectedType.parameters.indices) {
            val parameter = expectedType.parameters[i]
            lambdaScope.declareLocalVariable(parameterNames[i], parameter.type, false)
        }

        return parseClosureBody(tokenizer, context, expectedType, lambdaScope, expectedType.returnType, true, genericTypeMap)
    }



    fun parseFunctionExpression(tokenizer: TantillaScanner,
                                context: ParsingContext,
                                expectedType: FunctionType,
                                genericTypeMap: GenericTypeMap? = null,
    ): Node {
        if (tokenizer.current.text == "lambda") {
            return parseLambda(tokenizer, context, expectedType)
        }
        val functionScope = LambdaScope(context.scope) // resolvedType = type)
        for (i in expectedType.parameters.indices) {
            val parameter = expectedType.parameters[i]
            functionScope.declareLocalVariable("\$$i", parameter.type, false)
        }
        val body = TantillaExpressionParser.parseExpression(
            tokenizer,
            ParsingContext(functionScope, context.depth),
            null
        )
        if (body.returnType is FunctionType) {
            // TODO: Check that anonymous variables are not touched.
            val matchedBody = TantillaExpressionParser.matchType(body, expectedType, genericTypeMap)
            return FakeLambda(matchedBody)
        }

        val matchedBody =
            TantillaExpressionParser.matchType(body, expectedType.returnType, genericTypeMap)
        return LambdaReference(expectedType, functionScope.locals.size, matchedBody, implicit = true)
    }

}