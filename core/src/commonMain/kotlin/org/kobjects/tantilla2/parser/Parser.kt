package org.kobjects.tantilla2.parser

import org.kobjects.greenspun.core.*
import org.kobjects.parserlib.expressionparser.ExpressionParser
import org.kobjects.tantilla2.core.SymbolReference
import org.kobjects.tantilla2.core.*


fun String.unquote() = this.substring(1, this.length - 1)

object Parser {


    fun getIndent(s: String): Int {
        val lastBreak = s.lastIndexOf('\n')
        if (lastBreak == -1) {
            throw IllegalArgumentException("Line break expected")
        }
        return s.length - lastBreak
    }

    fun parse(s: String, context: ParsingContext): Evaluable<RuntimeContext> {
        val tokenizer = TantillaTokenizer(s)
        tokenizer.consume(TokenType.BOF);
        val result = parse(tokenizer, context)
        tokenizer.consume(TokenType.EOF)
        return result
    }

    fun parse(tokenizer: TantillaTokenizer, context: ParsingContext): Evaluable<RuntimeContext> {
        val statements = mutableListOf<Evaluable<RuntimeContext>>()
        while (tokenizer.current.type != TokenType.EOF) {
            if (tokenizer.tryConsume("def")) {
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Function name expected.")
                val text = consumeBody(tokenizer)
                context.defineFunction(name, text)
            } else {
                statements.add(parseStatement(tokenizer, context, 0))
            }
        }
        return if (statements.size == 1) statements[0]
            else Control.Block<RuntimeContext>(*statements.toTypedArray())
    }


    fun consumeBody(tokenizer: TantillaTokenizer): String {
        var depth = 1
        val pos = tokenizer.current.pos
        while (tokenizer.current.type != TokenType.EOF) {
            if (tokenizer.current.type == TokenType.LINE_BREAK) {
                depth = getIndent(tokenizer.current.text)
            } else {
                when (tokenizer.current.text) {
                    "def", "if", "while" -> depth++
                    "<|" -> {
                        tokenizer.next()
                        depth--
                    }
                }
            }
            if (depth == 0) {
                return tokenizer.input.substring(pos, tokenizer.current.pos)
            }
            tokenizer.next()
        }
        return tokenizer.input.substring(pos)
    }

    fun parseBlock(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
        returnDepth: Int
    ): Evaluable<RuntimeContext> {
        val statements = mutableListOf<Evaluable<RuntimeContext>>()
        var localDepth = returnDepth + 1
        while (tokenizer.current.type != TokenType.EOF && tokenizer.current.text != "<|") {
            if (tokenizer.current.type == TokenType.LINE_BREAK) {
                localDepth = getIndent(tokenizer.current.text)
                if (localDepth <= returnDepth) {
                    break
                }
                tokenizer.next()
            } else {
                statements.add(parseStatement(tokenizer, context, localDepth))
            }
        }
        return if (statements.size == 1) statements[0] else Control.Block(*statements.toTypedArray())
    }

    fun parseStatement(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
        currentDepth: Int
    ) : Evaluable<RuntimeContext> =
        if (tokenizer.tryConsume("if")) {
            parseIf(tokenizer, context, currentDepth)
        } else if (tokenizer.tryConsume("while")) {
            val condition = parseExpression(tokenizer, context)
            tokenizer.consume(":")
            Control.While(condition, parseBlock(tokenizer, context, currentDepth))
        } else if (tokenizer.tryConsume("var")) {
            parseVar(tokenizer, context)
        } else {
            parseExpression(tokenizer, context)
        }

    fun parseIf(tokenizer: TantillaTokenizer, context: ParsingContext, currentDepth: Int): Control.If<RuntimeContext> {
        val condition = parseExpression(tokenizer, context)
        tokenizer.consume(":")
        val then = parseBlock(tokenizer, context, currentDepth)
        return Control.If(condition, then)
    }

    fun parseVar(tokenizer: TantillaTokenizer, context: ParsingContext) : Evaluable<RuntimeContext> {
        val name = tokenizer.consume(TokenType.IDENTIFIER)
        tokenizer.consume("=")
        val initializer = parseExpression(tokenizer, context)
        val index = context.declareLocalVariable(name, initializer.type, true)
        return AssignLocal(index, initializer)
    }

    fun parseExpression(tokenizer: TantillaTokenizer, context: ParsingContext): Evaluable<RuntimeContext> =
        expressionParser.parse(tokenizer, context)

    fun parseFunctionType(tokenizer: TantillaTokenizer, context: ParsingContext): FunctionType {
        tokenizer.consume("(")
        val parameters = mutableListOf<Parameter>()
        if (!tokenizer.tryConsume(")")) {
            do {
                parameters.add(parseParameter(tokenizer, context))
            } while (tokenizer.tryConsume(","))
            tokenizer.consume(")", ", or ) expected here while parsing the parameter list.")
        }
        val returnType = if (tokenizer.tryConsume("->")) parseType(tokenizer, context) else Void
        return FunctionType(returnType, parameters)
    }

    fun parseLambda(tokenizer: TantillaTokenizer, context: ParsingContext): Lambda {
        val type = parseFunctionType(tokenizer, context)
        tokenizer.consume(":")
        val functionContext = ParsingContext(context)
        for (parameter in type.parameters) {
            functionContext.declareLocalVariable(parameter.name, parameter.type, mutable = false)
        }
        val body = parseBlock(tokenizer, functionContext, 0)
        return LambdaImpl(type, body)
    }

    fun parseParameter(tokenizer: TantillaTokenizer, context: ParsingContext): Parameter {
        val name = tokenizer.consume(TokenType.IDENTIFIER)
        tokenizer.consume(":", "Colon expected, separating the parameter type from the parameter name.")
        val type = parseType(tokenizer, context)
        return Parameter(name, type)
    }

    fun parseApply(tokenizer: TantillaTokenizer, context: ParsingContext, base: Evaluable<RuntimeContext>): Evaluable<RuntimeContext> {
        val arguments = mutableListOf<Evaluable<RuntimeContext>>()
        if (!tokenizer.tryConsume(")")) {
            do {
                arguments.add(parseExpression(tokenizer, context))
            } while (tokenizer.tryConsume(","))
            tokenizer.consume(")")
        }
        return Apply(base, arguments)
    }

    fun parsePrimary(tokenizer: TantillaTokenizer, context: ParsingContext): Evaluable<RuntimeContext> =
        when (tokenizer.current.type) {
            TokenType.NUMBER -> F64.Const(tokenizer.next().text.toDouble())
            TokenType.STRING -> Str.Const(tokenizer.next().text.unquote())
            TokenType.IDENTIFIER -> {
                val name = tokenizer.consume(TokenType.IDENTIFIER)
                val definition = context.resolve(name)
                when (definition.kind) {
                    Definition.Kind.LOCAL_VARIABLE -> LocalVariableReference(
                        name, definition.type(context), definition.index, definition.mutable)
                    Definition.Kind.CONST,
                    Definition.Kind.FUNCTION -> SymbolReference(name, definition.type(context), definition.value(context))
                }
            }
            else -> throw tokenizer.error("Number or identifier expected here.")
        }

    fun parseType(tokenizer: TantillaTokenizer, context: ParsingContext): Type {
        if (tokenizer.tryConsume("float")) {
            return F64
        }
        throw tokenizer.error("Unrecognized type.")
    }

    val expressionParser = ExpressionParser<TantillaTokenizer, ParsingContext, Evaluable<RuntimeContext>>(
        ExpressionParser.suffix(5, "(") {
                tokenizer, context, _, base -> parseApply(tokenizer, context, base) },
        ExpressionParser.infix(4, "*") { _, _, _, l, r -> F64.Mul(l, r)},
        ExpressionParser.infix(4, "/") { _, _, _, l, r -> F64.Div(l, r)},
        ExpressionParser.infix(4, "%") { _, _, _, l, r -> F64.Mod(l, r)},
        ExpressionParser.infix(3, "+") { _, _, _, l, r -> F64.Add(l, r)},
        ExpressionParser.infix(3, "-") { _, _, _, l, r -> F64.Sub(l, r)},
        ExpressionParser.infix(2, "==") { _, _, _, l, r -> F64.Eq(l, r)},
        ExpressionParser.infix(2, "!=") { _, _, _, l, r -> F64.Ne(l, r)},
        ExpressionParser.infix(1, "<") { _, _, _, l, r -> F64.Lt(l, r)},
        ExpressionParser.infix(1, ">") { _, _, _, l, r -> F64.Gt(l, r)},
        ExpressionParser.infix(1, "<=") { _, _, _, l, r -> F64.Le(l, r)},
        ExpressionParser.infix(1, ">=") { _, _, _, l, r -> F64.Ge(l, r)},

    ) { t, c ->
        parsePrimary(t, c)
    }
}