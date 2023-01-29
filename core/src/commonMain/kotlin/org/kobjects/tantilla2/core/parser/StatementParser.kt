package org.kobjects.tantilla2.core.parser

import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.function.FunctionDefinition
import org.kobjects.tantilla2.core.function.LocalVariableDefinition
import org.kobjects.tantilla2.core.node.*
import org.kobjects.tantilla2.core.collection.ListType
import org.kobjects.tantilla2.core.collection.RangeType
import org.kobjects.tantilla2.core.function.LambdaScope
import org.kobjects.tantilla2.core.type.NoneType
import org.kobjects.tantilla2.core.node.statement.*

object StatementParser {

    fun parseStatementFailsafe(
        tokenizer: TantillaScanner,
        context: ParsingContext,
        errors: MutableList<ParsingException>?,
    ) : Node {
        if (errors == null) {
            return parseStatement(tokenizer, context)
        }
        val startPos = tokenizer.current
        try {
            return parseStatement(tokenizer, context)
        } catch (e: Exception) {
            e.printStackTrace()
            val parsingException = tokenizer.ensureParsingException(e)
            errors.add(parsingException)
            val body = Parser.consumeBody(tokenizer, startPos, context.depth)
            return UnparseableStatement(body, parsingException)
        }
    }


    fun parseStatement(
        tokenizer: TantillaScanner,
        context: ParsingContext,
    ) : Node =
        when (tokenizer.current.text) {
            "for" -> parseFor(tokenizer, context)
      //      "if" -> parseIf(tokenizer, context)
            "let" -> parseLet(tokenizer, context)
            "return" -> parseReturn(tokenizer, context)
            "break" -> parseBreak(tokenizer, context)
   //         "while" -> parseWhile(tokenizer, context)
            else -> {
                if (tokenizer.current.type == TokenType.COMMENT) {
                    Comment(tokenizer.consume(TokenType.COMMENT).text)
                } else {
                    var expr = TantillaExpressionParser.parseExpression(tokenizer, context)
                    val text = tokenizer.current.text
                    if (text.endsWith("=") && text != "!=" && text != "<=" && text != ">=") {
                        tokenizer.consume()
                        while (tokenizer.current.type == TokenType.LINE_BREAK) {
                            tokenizer.consume()
                        }
                        val source = TantillaExpressionParser.parseExpression(tokenizer, context, expr.returnType)
                        expr = if (text == "=") Assignment(expr, source)
                        else CompoundAssignment.create(text, expr, source)
                    }
                    if (tokenizer.current.type != TokenType.EOF
                        && tokenizer.current.type != TokenType.LINE_BREAK
                        && !Parser.VALID_AFTER_STATEMENT.contains(tokenizer.current.text)
                    ) {
                        throw tokenizer.exception("Unexpected token ${tokenizer.current} after end of statement.")
                    }
                    expr
                }
            }
        }

    fun parseLet(tokenizer: TantillaScanner, context: ParsingContext): Node {
        tokenizer.consume("let")

        val mutable = tokenizer.tryConsume("mut")
        val name = tokenizer.consume(TokenType.IDENTIFIER).text
        val resolved = Parser.resolveVariable(tokenizer, context)

        val type = resolved.first
        val typeIsExplicit = resolved.second
        val initializer = resolved.third

        require (context.scope is FunctionDefinition || context.scope is LambdaScope) { "Local variables are allowed in function definitions only."}

        val definition = LocalVariableDefinition(
            context.scope, name, type = type, mutable = mutable)

        context.scope.add(definition)

        return Let(definition, type, typeIsExplicit, initializer)
    }

    fun parseBreak(tokenizer: TantillaScanner, context: ParsingContext): Node {
        tokenizer.consume("break")
        return FlowControlNode(FlowSignal.Kind.BREAK)
    }

    fun parseReturn(tokenizer: TantillaScanner, context: ParsingContext): Node {
        tokenizer.consume("return")
        // TODO: Fix this!
        var scope = context.scope
        while (scope is LambdaScope) {
            scope = scope.parentScope!!
        }

        if (scope !is FunctionDefinition) {
            throw tokenizer.exception("Function scope expected for 'return'")
        }
        if (scope.type.returnType == NoneType) {
            return FlowControlNode(FlowSignal.Kind.RETURN)
        }
        val expression = TantillaExpressionParser.parseExpression(tokenizer, context)
        return FlowControlNode(FlowSignal.Kind.RETURN, expression)
    }

    fun parseWhile(tokenizer: TantillaScanner, context: ParsingContext): WhileNode {
        tokenizer.consume("while")
        val condition = TantillaExpressionParser.parseExpression(tokenizer, context)
        tokenizer.consume(":")
        return WhileNode(condition, Parser.parseStatements(tokenizer, context.indent()))
    }

    fun parseFor(tokenizer: TantillaScanner, context: ParsingContext): ForNode {
        tokenizer.consume("for")
        val iteratorName = tokenizer.consume(TokenType.IDENTIFIER) { "Loop variable name expected." }.text
        tokenizer.consume("in")
        val iterableExpression = TantillaExpressionParser.parseExpression(tokenizer, context)
        tokenizer.consume(":")
        val iterableType = iterableExpression.returnType
        val iteratorType = when (iterableType) {
            RangeType -> org.kobjects.tantilla2.core.type.IntType
            is ListType -> iterableType.elementType
            else -> throw RuntimeException("Can't iterate type $iterableType")
        }

        val iteratorIndex = context.scope.declareLocalVariable(
            iteratorName, iteratorType, false)
        val body = Parser.parseStatements(tokenizer, context.indent())
        return ForNode(iteratorName, iteratorIndex, iterableExpression, body)
    }

    fun parseIf(tokenizer: TantillaScanner, context: ParsingContext): IfNode {
        tokenizer.consume("if")
        val expressions = mutableListOf<Node>()
        do {
            val condition = TantillaExpressionParser.parseExpression(tokenizer, context)
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

        return IfNode(*expressions.toTypedArray())
    }

}