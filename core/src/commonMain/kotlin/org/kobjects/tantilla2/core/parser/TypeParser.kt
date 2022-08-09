package org.kobjects.tantilla2.core.parser

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.classifier.StructDefinition
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.runtime.ListType
import org.kobjects.tantilla2.core.runtime.Void

object TypeParser {



    fun parseType(tokenizer: TantillaTokenizer, context: ParsingContext): Type {
        if (tokenizer.tryConsume("float")) {
            return org.kobjects.tantilla2.core.runtime.F64
        }
        if (tokenizer.tryConsume("str")) {
            return org.kobjects.tantilla2.core.runtime.Str
        }
        val name = tokenizer.consume(TokenType.IDENTIFIER)
        if (name.equals("List")) {
            tokenizer.consume("[")
            val elementType = parseType(tokenizer, context)
            tokenizer.consume("]")
            return ListType(elementType)
        }
        return context.scope.resolveStatic(name, true)!!.getValue(null) as Type
    }


    fun parseParameter(tokenizer: TantillaTokenizer, context: ParsingContext): Parameter {
        val isVararg = tokenizer.tryConsume("*")
        val name = tokenizer.consume(TokenType.IDENTIFIER)
        tokenizer.consume(":", "Colon expected, separating the parameter type from the parameter name.")
        val rawType = TypeParser.parseType(tokenizer, context)
        val type = if (isVararg) ListType(rawType) else rawType
        val defaultValue: Evaluable<LocalRuntimeContext>? = if (tokenizer.tryConsume("="))
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
        val returnType = if (tokenizer.tryConsume("->")) parseType(tokenizer, context) else Void
        return FunctionType.Impl(returnType, parameters)
    }
}