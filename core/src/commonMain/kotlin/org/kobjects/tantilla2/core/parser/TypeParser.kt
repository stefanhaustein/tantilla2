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
import org.kobjects.tantilla2.core.collection.MutableListType
import org.kobjects.tantilla2.core.type.VoidType

object TypeParser {



    fun parseType(tokenizer: TantillaTokenizer, context: ParsingContext): Type {
        if (tokenizer.tryConsume("float")) {
            return org.kobjects.tantilla2.core.type.FloatType
        }
        if (tokenizer.tryConsume("str")) {
            return org.kobjects.tantilla2.core.type.StrType
        }
        var name = tokenizer.consume(TokenType.IDENTIFIER)
        if (name.equals("List") || name.equals("MutableList")) {
            tokenizer.consume("[")
            val elementType = parseType(tokenizer, context)
            tokenizer.consume("]")
            return if (name == "List") ListType(elementType) else MutableListType(elementType)
        }
        var scope = context.scope
        while (tokenizer.tryConsume(".")) {
            scope = scope.resolveStaticOrError(name, scope == context.scope).getValue(null) as Scope
            name = tokenizer.consume(TokenType.IDENTIFIER)
        }

        return scope.resolveStaticOrError(name, scope == context.scope).getValue(null) as Type
    }


    fun parseParameter(tokenizer: TantillaTokenizer, context: ParsingContext): Parameter {
        val isVararg = tokenizer.tryConsume("*")
        val name = tokenizer.consume(TokenType.IDENTIFIER)
        tokenizer.consume(":", "Colon expected, separating the parameter type from the parameter name.")
        val rawType = TypeParser.parseType(tokenizer, context)
        val type = if (isVararg) ListType(rawType) else rawType
        val defaultValue: Node? = if (tokenizer.tryConsume("="))
            ExpressionParser.matchType(context.scope,
                ExpressionParser.parseExpression(tokenizer, context), type)
        else null

        return Parameter(name, type, defaultValue, isVararg)
    }

    fun parseFunctionType(tokenizer: TantillaTokenizer, context: ParsingContext, isMethod: Boolean): FunctionType {
        tokenizer.consume("(")
        val parameters = mutableListOf<Parameter>()
        if (isMethod) {
            val selfType: Type = when (context.scope) {
                is StructDefinition -> context.scope
                is TraitDefinition -> context.scope
                is ImplDefinition -> context.scope.scope as Type
                else ->
                    throw IllegalStateException("self supported for classes, traits and implemenetations only; got: ${context}")
            }
            parameters.add(Parameter("self", selfType, null))
        }
        if (!tokenizer.tryConsume(")")) {
            do {
                parameters.add(parseParameter(tokenizer, context))
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
        val returnType = if (tokenizer.tryConsume("->")) parseType(tokenizer, context) else VoidType
        return FunctionType.Impl(returnType, parameters)
    }
}