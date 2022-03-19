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
        val result = parseRoot(tokenizer, context)
        tokenizer.consume(TokenType.EOF)
        return result
    }

    fun parseRoot(tokenizer: TantillaTokenizer, context: ParsingContext): Evaluable<RuntimeContext> {
        val statements = mutableListOf<Evaluable<RuntimeContext>>()
        while (tokenizer.current.type != TokenType.EOF) {
            if (tokenizer.tryConsume("def")) {
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Function name expected.")
                val text = consumeBody(tokenizer)
                context.defineFunction(name, text)
            } else if (tokenizer.tryConsume("class")) {
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Class name expected.")
                val text = consumeBody(tokenizer)
                context.defineClass(name, text)
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
                    "def", "if", "while", "class" -> depth++
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

        println("parsing block with local depth: $localDepth return depth $returnDepth")

        while (tokenizer.current.type != TokenType.EOF && tokenizer.current.text != "<|") {
            if (tokenizer.current.type == TokenType.LINE_BREAK) {
                localDepth = getIndent(tokenizer.current.text)
                println("line break with depth $localDepth")
                if (localDepth <= returnDepth) {
                    break
                }
                tokenizer.next()
            } else {
                val statement = parseStatement(tokenizer, context, localDepth)
                println("adding statement to block: $statement")
                statements.add(statement)
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
            val expr = parseExpression(tokenizer, context)
            if (tokenizer.tryConsume("=")) {
                if (expr !is Assignable) {
                    tokenizer.error("Target is not assignable")
                }
                Assignment(expr as Assignable, parseExpression(tokenizer, context))
            } else {
                expr
            }
        }

    fun skipLineBreaks(tokenizer: TantillaTokenizer, currentDepth: Int) {
        while (tokenizer.current.type == TokenType.LINE_BREAK
            && getIndent(tokenizer.current.text) >= currentDepth) {
            tokenizer.next()
        }
    }

    fun parseIf(tokenizer: TantillaTokenizer, context: ParsingContext, currentDepth: Int): Control.If<RuntimeContext> {
        val expressions = mutableListOf<Evaluable<RuntimeContext>>()
        println("parsing if at depth $currentDepth")
        do {
            val condition = parseExpression(tokenizer, context)
            println("parsed (el)if condition at depth $currentDepth: $condition")
            expressions.add(condition)
            tokenizer.consume(":")
            val block = parseBlock(tokenizer, context, currentDepth)
            println("parsed block at depth $currentDepth: $block")
            expressions.add(block)
            skipLineBreaks(tokenizer, currentDepth)
        } while (tokenizer.tryConsume("elif"))

        if (tokenizer.tryConsume("else")) {
            println("Consumend else at level $currentDepth")
            tokenizer.consume(":")
            val otherwise = parseBlock(tokenizer, context, currentDepth)
            println("parsed else at depth $currentDepth: $otherwise")
            expressions.add(otherwise)
        }

        return Control.If(*expressions.toTypedArray())
    }

    fun parseVar(tokenizer: TantillaTokenizer, context: ParsingContext) : Evaluable<RuntimeContext> {
        val name = tokenizer.consume(TokenType.IDENTIFIER)
        tokenizer.consume("=")
        val initializer = parseExpression(tokenizer, context)
        val index = context.declareLocalVariable(name, initializer.type, true)
        return Assignment(LocalVariableReference(name, initializer.type, index, true), initializer)
    }

    fun parseExpression(tokenizer: TantillaTokenizer, context: ParsingContext): Evaluable<RuntimeContext> =
        expressionParser.parse(tokenizer, context)

    fun parseFunctionType(tokenizer: TantillaTokenizer, context: ParsingContext): FunctionType {
        tokenizer.consume("(")
        val parameters = mutableListOf<Parameter>()
        var isMethod = false
        if (!tokenizer.tryConsume(")")) {
            if (tokenizer.tryConsume("self")) {
                if (context.parentContext?.kind != ParsingContext.Kind.CLASS) {
                    throw IllegalStateException("self supported for classes only.")
                }
                isMethod = true
                parameters.add(Parameter("self", context.parentContext))
                while (tokenizer.tryConsume(",")) {
                    parameters.add(parseParameter(tokenizer, context))
                }
            } else {
                do {
                    parameters.add(parseParameter(tokenizer, context))
                } while (tokenizer.tryConsume(","))
            }
            tokenizer.consume(")", ", or ) expected here while parsing the parameter list.")
        }
        val returnType = if (tokenizer.tryConsume("->")) parseType(tokenizer, context) else Void
        return FunctionType(isMethod, returnType, parameters)
    }

    fun parseLambda(tokenizer: TantillaTokenizer, context: ParsingContext): Lambda {
        val type = parseFunctionType(tokenizer, context)
        tokenizer.consume(":")
        val functionContext = ParsingContext(
            "",
            if (type.isMethod) ParsingContext.Kind.METHOD else ParsingContext.Kind.FUNCTION,
            context)
        for (parameter in type.parameters) {
            functionContext.declareParameter(parameter.name, parameter.type)
        }
        functionContext.body = parseBlock(tokenizer, functionContext, 0)
        functionContext.returnType = type.returnType
        return functionContext
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

    fun consumeAndResoloveIdentifier(tokenizer: TantillaTokenizer, context: ParsingContext): Definition {
        val name = tokenizer.consume(TokenType.IDENTIFIER)
        try {
            return context.resolve(name)
        } catch (e: Exception) {
            throw tokenizer.error(e.message ?: "Can't resolve $name")
        }
    }

    fun parsePrimary(tokenizer: TantillaTokenizer, context: ParsingContext): Evaluable<RuntimeContext> =
        when (tokenizer.current.type) {
            TokenType.NUMBER -> F64.Const(tokenizer.next().text.toDouble())
            TokenType.STRING -> Str.Const(tokenizer.next().text.unquote())
            TokenType.IDENTIFIER -> {
                val definition = consumeAndResoloveIdentifier(tokenizer, context)
                when (definition.kind) {
                    Definition.Kind.LOCAL_VARIABLE -> LocalVariableReference(
                       definition.name, definition.type(context), definition.index, definition.mutable)
                    Definition.Kind.CLASS,
                    Definition.Kind.CONST,
                    Definition.Kind.FUNCTION -> SymbolReference(
                        definition.name, definition.type(context), definition.value(context))
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