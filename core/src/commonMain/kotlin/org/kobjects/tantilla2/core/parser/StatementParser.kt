package org.kobjects.tantilla2.core.parser

import org.kobjects.greenspun.core.Control
import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.FunctionDefinition
import org.kobjects.tantilla2.core.function.LocalVariableDefinition
import org.kobjects.tantilla2.core.node.*
import org.kobjects.tantilla2.core.runtime.ListType
import org.kobjects.tantilla2.core.runtime.RangeType
import org.kobjects.tantilla2.core.runtime.Void

object StatementParser {

    fun parseStatement(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
    ) : Evaluable<LocalRuntimeContext> =
        when (tokenizer.current.text) {
            "for" -> parseFor(tokenizer, context)
            "if" -> parseIf(tokenizer, context)
            "let" -> parseLet(tokenizer, context)
            "return" -> parseReturn(tokenizer, context)
            "while" -> parseWhile(tokenizer, context)
            else -> {
                var expr = ExpressionParser.parseExpression(tokenizer, context)
                if (tokenizer.tryConsume("=")) {
                    if (expr !is Assignable) {
                        tokenizer.exception("Target is not assignable")
                    }
                    expr = Assignment(expr as Assignable, ExpressionParser.parseExpression(tokenizer, context))
                }
                if (tokenizer.current.type != TokenType.EOF
                    && tokenizer.current.type != TokenType.LINE_BREAK
                    && !Parser.VALID_AFTER_STATEMENT.contains(tokenizer.current.text)) {
                    throw tokenizer.exception("Unexpected token ${tokenizer.current} after end of statement.")
                }
                expr
            }
        }

    fun parseLet(tokenizer: TantillaTokenizer, context: ParsingContext): Evaluable<LocalRuntimeContext> {
        tokenizer.consume("let")

        val mutable = tokenizer.tryConsume("mut")
        val name = tokenizer.consume(TokenType.IDENTIFIER)
        val resolved = Parser.resolveVariable(tokenizer, context)

        val type = resolved.first
        val typeIsExplicit = resolved.second
        val initializer = resolved.third

        val definition = LocalVariableDefinition(
            context.scope, name, type = type, mutable = mutable)

        context.scope.add(definition)

        return Let(definition, type, typeIsExplicit, initializer)
    }



    fun parseReturn(tokenizer: TantillaTokenizer, context: ParsingContext): Evaluable<LocalRuntimeContext> {
        tokenizer.consume("return")
        if (context.scope !is FunctionDefinition) {
            throw tokenizer.exception("Function scope expected for 'return'")
        }
        if (context.scope.type.returnType == Void) {
            return FlowControl(Control.FlowSignal.Kind.RETURN)
        }
        val expression = ExpressionParser.parseExpression(tokenizer, context)
        return FlowControl(Control.FlowSignal.Kind.RETURN, expression)
    }

    fun parseWhile(tokenizer: TantillaTokenizer, context: ParsingContext): Control.While<LocalRuntimeContext> {
        tokenizer.consume("while")
        val condition = ExpressionParser.parseExpression(tokenizer, context)
        tokenizer.consume(":")
        return Control.While(condition, Parser.parseDefinitionsAndStatements(tokenizer, context.indent()))
    }

    fun parseFor(tokenizer: TantillaTokenizer, context: ParsingContext): For {
        tokenizer.consume("for")
        val iteratorName = tokenizer.consume(TokenType.IDENTIFIER, "Loop variable name expected.")
        tokenizer.consume("in")
        val iterableExpression = ExpressionParser.parseExpression(tokenizer, context)
        tokenizer.consume(":")
        val iterableType = iterableExpression.returnType
        val iteratorType = when (iterableType) {
            RangeType -> org.kobjects.tantilla2.core.runtime.F64
            is ListType -> iterableType.elementType
            else -> throw RuntimeException("Can't iterate type $iterableType")
        }

        val iteratorIndex = context.scope.declareLocalVariable(
            iteratorName, iteratorType, false)
        val body = Parser.parseStatements(tokenizer, context.indent())
        return For(iteratorName, iteratorIndex, iterableExpression, body)
    }

    fun parseIf(tokenizer: TantillaTokenizer, context: ParsingContext): Control.If<LocalRuntimeContext> {
        tokenizer.consume("if")
        val expressions = mutableListOf<Evaluable<LocalRuntimeContext>>()
        do {
            val condition = ExpressionParser.parseExpression(tokenizer, context)
            expressions.add(condition)
            tokenizer.consume(":")
            val block = Parser.parseStatements(tokenizer, context.indent())
            expressions.add(block)
            Parser.skipLineBreaks(tokenizer, context.depth)
        } while (tokenizer.tryConsume("elif"))

        if (tokenizer.tryConsume("else")) {
            tokenizer.consume(":")
            val otherwise = Parser.parseStatements(tokenizer, context.indent())
            expressions.add(otherwise)
        }

        return Control.If(*expressions.toTypedArray())
    }

}