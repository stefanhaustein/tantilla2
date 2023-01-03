package org.kobjects.tantilla2.core.parser

import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.classifier.StructDefinition
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.collection.ListType
import org.kobjects.tantilla2.core.type.VoidType

object TypeParser {


    fun parseType(tokenizer: TantillaTokenizer, context: ParsingContext): Type {
        if (tokenizer.current.text == "(") {
            return parseFunctionType(tokenizer, context, false)
        }
        var name = tokenizer.consume(TokenType.IDENTIFIER)
        var scope = context.scope

        // Note that getValue() below is required to resolve imports!
        while (tokenizer.tryConsume(".")) {
            scope = scope.resolveStaticOrError(name, scope == context.scope).getValue(null) as Scope
            name = tokenizer.consume(TokenType.IDENTIFIER)
        }
        val type = scope.resolveStaticOrError(name, scope == context.scope).getValue(null) as Type

        if (type.genericParameterTypes.isNotEmpty() && tokenizer.tryConsume("[")) {
            tokenizer.disable(TokenType.LINE_BREAK)
            val arguments = mutableListOf<Type>()
            do {
                arguments.add(parseType(tokenizer, context))
            } while (tokenizer.tryConsume(","))
            tokenizer.consume("]")
            tokenizer.enable(TokenType.LINE_BREAK)
            return type.withGenericsResolved(arguments)
        }

        return type
    }


    fun parseParameter(tokenizer: TantillaTokenizer, context: ParsingContext, index: Int): Parameter {
        val isVararg = tokenizer.tryConsume("*")
        val name: String
        if (tokenizer.current.type == TokenType.IDENTIFIER && tokenizer.lookAhead(1).text == ":") {
            name = tokenizer.consume(TokenType.IDENTIFIER)
            tokenizer.consume(
                ":",
                "Colon expected, separating the parameter type from the parameter name."
            )
        } else {
            name = "\$$index"
        }
        val rawType = parseType(tokenizer, context)
        val type = if (isVararg) ListType(rawType) else rawType
        val defaultValue: Node? = if (tokenizer.tryConsume("="))
            ExpressionParser.matchType(context.scope,
                ExpressionParser.parseExpression(tokenizer, context), type)
        else null

        return Parameter(name, type, defaultValue, isVararg)
    }

    fun parseFunctionType(tokenizer: TantillaTokenizer, context: ParsingContext, isMethod: Boolean): FunctionType {
        tokenizer.consume("(")
        tokenizer.disable(TokenType.LINE_BREAK)
        val parameters = mutableListOf<Parameter>()
        if (isMethod) {
            val selfType: Type = when (context.scope) {
                is StructDefinition -> context.scope
                is TraitDefinition -> context.scope
                is ImplDefinition -> if (context.scope.scope is Type) context.scope.scope as Type else VoidType
                else ->
                    throw IllegalStateException("self supported for structs, traits and implementations only; got: ${context}")
            }
            parameters.add(Parameter("self", selfType, null))
        }
        if (!tokenizer.tryConsume(")")) {
            var index = 0
            do {
                parameters.add(parseParameter(tokenizer, context, index++))
            } while (tokenizer.tryConsume(","))
            var varargCount = 0
            for (parameter in parameters) {
                if (parameter.isVararg) {
                    varargCount++
                    if (varargCount > 1) {
                        throw IllegalArgumentException("Only one vararg parameter allowed.")
                    }
                }
            }

            tokenizer.consume(")", ", or ) expected here while parsing the parameter list.")
        }
        tokenizer.enable(TokenType.LINE_BREAK)
        val returnType = if (tokenizer.tryConsume("->")) parseType(tokenizer, context) else VoidType
        return FunctionType.Impl(returnType, parameters)
    }
}