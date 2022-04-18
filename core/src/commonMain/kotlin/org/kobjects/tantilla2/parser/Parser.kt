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
        return s.length - lastBreak - 1
    }

    fun parse(s: String, context: ParsingContext): Evaluable<RuntimeContext> {
        val tokenizer = TantillaTokenizer(s)
        tokenizer.consume(TokenType.BOF);
        val result = parse(tokenizer, context)
        tokenizer.consume(TokenType.EOF)
        return result
    }

    fun parse(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
        returnDepth: Int = -1,
    ): Evaluable<RuntimeContext> {
        val statements = mutableListOf<Evaluable<RuntimeContext>>()
        var localDepth = returnDepth + 1
        while (tokenizer.current.type != TokenType.EOF && tokenizer.current.text != "<|") {
            if (tokenizer.current.type == TokenType.LINE_BREAK) {
                localDepth = getIndent(tokenizer.current.text)
                println("line break with depth $localDepth")
                if (localDepth <= returnDepth) {
                    break
                }
                tokenizer.next()
            } else if (tokenizer.tryConsume("var")) {
                statements.add(parseLocalVariable(tokenizer, context, true))
            } else if (tokenizer.tryConsume("let")) {
                statements.add(parseLocalVariable(tokenizer, context, false))
            } else if (tokenizer.tryConsume("def")) {
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Function name expected.")
                println("consuming def $name")
                val text = consumeBody(tokenizer, localDepth)
                context.defineDelayed(Definition.Kind.FUNCTION, name, text)
            } else if (tokenizer.tryConsume("class")) {
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Class name expected.")
                println("consuming class $name; return depth: $localDepth")
                val text = consumeBody(tokenizer, localDepth)
                context.defineDelayed(Definition.Kind.CLASS, name, text)
            } else if (tokenizer.tryConsume("trait")) {
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Trait name expected.")
                println("consuming trait $name; return depth: $localDepth")
                val text = consumeBody(tokenizer, localDepth)
                context.defineDelayed(Definition.Kind.TRAIT, name, text)
            } else if (tokenizer.tryConsume("impl")) {
                val traitName = tokenizer.consume(TokenType.IDENTIFIER, "Trait name expected.")
                tokenizer.consume("for")
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Class name expected.")
                println("consuming impl $traitName for $name; return depth: $localDepth")
                val text = consumeBody(tokenizer, localDepth)
                context.defineDelayed(Definition.Kind.IMPL, "$traitName for $name", text)
            } else {
                val statement = parseStatement(tokenizer, context, localDepth)
                println("parsed statement: $statement")
                statements.add(statement)
            }
        }
        return if (statements.size == 1) statements[0]
            else Control.Block<RuntimeContext>(*statements.toTypedArray())
    }

    fun consumeBody(
        tokenizer: TantillaTokenizer,
        returnDepth: Int = -1
    ): String {
        var localDepth = returnDepth + 1
        val pos = tokenizer.current.pos
        while (tokenizer.current.type != TokenType.EOF) {
            if (tokenizer.current.type == TokenType.LINE_BREAK) {
                localDepth = getIndent(tokenizer.current.text)
                println("- new local depth: $localDepth")
            } else {
                when (tokenizer.current.text) {
                    "def", "if", "while", "class" -> localDepth++
                    "<|" -> localDepth--
                }
            }
            if (localDepth <= returnDepth) {
                return tokenizer.input.substring(pos, tokenizer.current.pos)
            }
            tokenizer.next()
        }
        return tokenizer.input.substring(pos)
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
            Control.While(condition, parse(tokenizer, context, currentDepth))
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
            val block = parse(tokenizer, context, currentDepth)
            println("parsed block at depth $currentDepth: $block")
            expressions.add(block)
            skipLineBreaks(tokenizer, currentDepth)
        } while (tokenizer.tryConsume("elif"))

        if (tokenizer.tryConsume("else")) {
            println("Consumend else at level $currentDepth")
            tokenizer.consume(":")
            val otherwise = parse(tokenizer, context, currentDepth)
            println("parsed else at depth $currentDepth: $otherwise")
            expressions.add(otherwise)
        }

        return Control.If(*expressions.toTypedArray())
    }

    fun parseLocalVariable(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
        mutable: Boolean,
    ) : Evaluable<RuntimeContext> {
        val name = tokenizer.consume(TokenType.IDENTIFIER)
        var type: Type? = null
        if (tokenizer.tryConsume(":")) {
            type = parseType(tokenizer, context)
        }
        var initializer: Evaluable<RuntimeContext>? = null
        val asParameter = context.kind == ParsingContext.Kind.CLASS
        if (tokenizer.tryConsume("=")) {
            if (asParameter) {
                throw IllegalStateException("Parameter initializers NYI")
            }
            initializer = parseExpression(tokenizer, context)
            if (type != null && type.isAssignableFrom(initializer.type)) {
                throw IllegalStateException("Initializer type mismatch: ${initializer.type} can't be assigned to $type")
            }
            type = initializer.type
        } else if (type == null) {
            throw IllegalStateException("Explicit type or initializer expression required.")
        }
        val index = context.declareLocalVariable(name, type, mutable)
        return if (initializer == null) Control.Block<RuntimeContext>()
            else Assignment(LocalVariableReference(name, type, index, mutable), initializer)
    }

    fun parseExpression(tokenizer: TantillaTokenizer, context: ParsingContext): Evaluable<RuntimeContext> =
        expressionParser.parse(tokenizer, context)

    fun parseFunctionType(tokenizer: TantillaTokenizer, context: ParsingContext): FunctionType {
        tokenizer.consume("(")
        val parameters = mutableListOf<Parameter>()
        var isMethod = false
        if (!tokenizer.tryConsume(")")) {
            if (tokenizer.tryConsume("self")) {
                if (context.kind != ParsingContext.Kind.CLASS) {
                    throw IllegalStateException("self supported for classes only; got: ${context.kind}")
                }
                isMethod = true
                parameters.add(Parameter("self", context))
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
        return FunctionType(returnType, parameters)
    }

    fun parseLambda(tokenizer: TantillaTokenizer, context: ParsingContext): Lambda {
        val type = parseFunctionType(tokenizer, context)
        if (context.kind == ParsingContext.Kind.TRAIT) {
            tokenizer.consume(TokenType.EOF, "Trait methods must not have function bodies.")
            return TraitMethod(type, context.traitIndex++)
        }

        tokenizer.consume(":")
        val functionContext = ParsingContext(
            "",
            ParsingContext.Kind.FUNCTION,
            context)
        for (parameter in type.parameters) {
            functionContext.declareLocalVariable(parameter.name, parameter.type, false)
        }
        val body = parse(tokenizer, functionContext, 0)
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
                val argument = parseExpression(tokenizer, context)
                println("Parsed argument: $argument")
                arguments.add(argument)
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
                            definition.name,
                            definition.type(),
                            definition.index,
                            definition.mutable
                        )
                        Definition.Kind.CLASS,
                            Definition.Kind.TRAIT,
                        Definition.Kind.CONST,
                            Definition.Kind.IMPL,
                        Definition.Kind.FUNCTION -> SymbolReference(
                            definition.name, definition.type(), definition.value()
                        )
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

    fun parseList(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
        endMarker: String): List<Evaluable<RuntimeContext>> {
        val result = mutableListOf<Evaluable<RuntimeContext>>()
        if (tokenizer.current.text != endMarker) {
            do {
                result.add(parseExpression(tokenizer, context))
            } while (tokenizer.tryConsume(","))
        }
        tokenizer.consume(endMarker, ") or , expected")
        return result.toList()
    }

    fun parseProperty(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
        base: Evaluable<RuntimeContext>,
    ): Evaluable<RuntimeContext> {
        val name = tokenizer.consume(TokenType.IDENTIFIER)
        if (!(base.type is ParsingContext)) {
            tokenizer.error("Base type must be parsing context; got: ${base.type} for $base.")
        }
        val baseType = base.type as ParsingContext
        val definition = baseType.resolve(name)

        when (definition.kind) {
            Definition.Kind.LOCAL_VARIABLE ->
            return PropertyReference(
                base,
                name,
                definition.type(),
                definition.index,
                definition.mutable
            )
            Definition.Kind.FUNCTION -> {
                val fn = SymbolReference(name, definition.type(), definition.value())
                val args = if (tokenizer.tryConsume("(")) parseList(tokenizer, context, ")")
                    else emptyList()
                val params = List<Evaluable<RuntimeContext>>(args.size + 1) {
                    if (it == 0) base else args[it - 1]
                }
                return Apply(fn, params)
            }
            else -> throw tokenizer.error("Unsupported definition kind ${definition.kind} for $base.$name")
        }
    }

    val expressionParser = ExpressionParser<TantillaTokenizer, ParsingContext, Evaluable<RuntimeContext>>(
        ExpressionParser.suffix(6, ".") {
                tokenizer, context, _, base -> parseProperty(tokenizer, context, base) },
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