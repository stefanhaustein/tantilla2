package org.kobjects.tantilla2.core.parser

import org.kobjects.greenspun.core.*
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.classifier.*
import org.kobjects.tantilla2.core.function.*


fun String.unquote() = this.substring(1, this.length - 1)

object Parser {
    val DECLARATION_KEYWORDS = setOf("def", "struct", "trait", "impl", "static", "mut")

    val VALID_AFTER_STATEMENT = setOf(")", ",", "]", "}", "<|")

    fun getIndent(s: String): Int {
        val lastBreak = s.lastIndexOf('\n')
        if (lastBreak == -1) {
            throw IllegalArgumentException("Line break expected")
        }
        return s.length - lastBreak - 1
    }

    fun parse(s: String, context: Scope): Evaluable<RuntimeContext> {
        val tokenizer = TantillaTokenizer(s)
        tokenizer.consume(TokenType.BOF);
        val result = parse(tokenizer, ParsingContext(context, 0))
        tokenizer.consume(TokenType.EOF)
        return result
    }

    fun parse(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
    ): Evaluable<RuntimeContext> {
        val statements = mutableListOf<Evaluable<RuntimeContext>>()
        val scope = context.scope
        var localDepth = context.depth
        while (tokenizer.current.type != TokenType.EOF
            && !VALID_AFTER_STATEMENT.contains(tokenizer.current.text)
        ) {
            if (tokenizer.current.type == TokenType.LINE_BREAK) {
                localDepth = getIndent(tokenizer.current.text)
                println("line break with depth $localDepth")
                if (localDepth < context.depth) {
                    break
                }
                tokenizer.next()
            } else if (DECLARATION_KEYWORDS.contains(tokenizer.current.text) ||
                (context.scope !is FunctionDefinition
                        && tokenizer.current.type == TokenType.IDENTIFIER
                        && (tokenizer.lookAhead(1).text == ":" || tokenizer.lookAhead(1).text == "="))) {
                    val definition = parseDefinition(tokenizer, ParsingContext(scope, localDepth))
                    scope.definitions.add(definition)
                } else {
                    val statement = StatementParser.parseStatement(tokenizer, ParsingContext(scope, localDepth))
                    statements.add(statement)
            }
        }
        return if (statements.size == 1) statements[0]
            else Control.Block<RuntimeContext>(*statements.toTypedArray())
    }

    fun readDocString(tokenizer: TantillaTokenizer): String {
        if (tokenizer.current.type == TokenType.STRING || tokenizer.current.type == TokenType.MULTILINE_STRING) {
            return tokenizer.next().text
        }
        return ""
    }

    fun parseDefinition(tokenizer: TantillaTokenizer, context: ParsingContext): Definition {
        val startPos = tokenizer.current.pos
        val explicitlyStatic = tokenizer.tryConsume("static")
        val mutable = tokenizer.tryConsume("mut")
        val scope = context.scope

        if (tokenizer.current.type == TokenType.IDENTIFIER
            && (tokenizer.lookAhead(1).text == "=" || tokenizer.lookAhead(1).text == ":")) {
            val local = !explicitlyStatic && (scope is StructDefinition || scope is FunctionDefinition)
            return parseFieldDeclaration(tokenizer, context, startPos, local, mutable)
        }

        return when (val kind = tokenizer.current.text) {
            "def" -> {
                val isMethod = !explicitlyStatic && (scope is StructDefinition || scope is TraitDefinition ||
                        scope is ImplDefinition)
                tokenizer.consume("def")
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Function name expected.")
                println("consuming def $name")
                val text = consumeBody(tokenizer, startPos, context.depth)
                FunctionDefinition(context.scope, if (isMethod) Definition.Kind.METHOD else Definition.Kind.FUNCTION, name, definitionText = text)
            }
            "struct",
            "trait"-> {
                tokenizer.consume(kind)
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Struct name expected.")
                tokenizer.consume(":")
                val docString = readDocString(tokenizer)
                val definition = if (kind == "struct") StructDefinition(context.scope, name, docString = docString)
                else TraitDefinition(context.scope, name, docString = docString)
                parseScopeBody(tokenizer, ParsingContext(definition, context.depth + 1))
                definition
            }
            "impl" -> {
                tokenizer.consume("impl")
                val traitName = tokenizer.consume(TokenType.IDENTIFIER, "Trait name expected.")
                tokenizer.consume("for")
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Class name expected.")
                tokenizer.consume(":")
                val docString = readDocString(tokenizer)
                val text = consumeBody(tokenizer, startPos, context.depth)
                ImplDefinition(
                    context.scope,
                    "$traitName for $name",
                    definitionText = text,
                    docString = docString
                )
            }
            else -> throw tokenizer.exception("Declaration expected.")
        }
    }

    fun parseScopeBody(tokenizer: TantillaTokenizer, parsingContext: ParsingContext): Scope {
        parse(tokenizer, parsingContext)
        return parsingContext.scope
    }

    fun consumeLine(tokenizer: TantillaTokenizer, startPos: Int): String {
        while (tokenizer.current.type != TokenType.EOF && tokenizer.current.type != TokenType.LINE_BREAK) {
            tokenizer.next()
        }
        return tokenizer.input.substring(startPos, tokenizer.current.pos)
    }

    fun consumeBody(
        tokenizer: TantillaTokenizer,
        startPos: Int,
        returnDepth: Int
    ): String {
        var localDepth = returnDepth + 1
        val pos = tokenizer.current.pos
        while (tokenizer.current.type != TokenType.EOF) {
            if (tokenizer.current.type == TokenType.LINE_BREAK) {
                localDepth = getIndent(tokenizer.current.text)
                println("- new local depth: $localDepth")
            } else {
                when (tokenizer.current.text) {
                    "def", "if", "while", "struct", "impl", "trait" -> localDepth++
                    "<|" -> localDepth--
                }
            }
            if (localDepth <= returnDepth) {
                return tokenizer.input.substring(startPos, tokenizer.current.pos)
            }
            tokenizer.next()
        }
        return tokenizer.input.substring(startPos)
    }

    fun skipLineBreaks(tokenizer: TantillaTokenizer, currentDepth: Int) {
        while (tokenizer.current.type == TokenType.LINE_BREAK
            && getIndent(tokenizer.current.text) >= currentDepth) {
            tokenizer.next()
        }
    }


    fun resolveVariable(tokenizer: TantillaTokenizer, context: ParsingContext, typeOnly: Boolean = false):
            Triple<Type, Boolean, Evaluable<RuntimeContext>?> {

        val scope = context.scope
        var type: Type? = null
        var initializer: Evaluable<RuntimeContext>? = null
        var typeIsExplicit = tokenizer.tryConsume(":")
        if (typeIsExplicit) {
            type = TypeParser.parseType(tokenizer, ParsingContext(scope, 0))
            if (typeOnly) {
                return Triple(type, true, null)
            }
        }
        if (tokenizer.tryConsume("=")) {
            initializer = ExpressionParser.parseExpression(tokenizer, context)
            if (type == null) {
                type = initializer.returnType
            } else {
                initializer = ExpressionParser.matchType(scope, initializer, type)
            }
        } else if (type == null) {
            throw tokenizer.exception("Explicit type or initializer expression required.")
        }
        return Triple(type, typeIsExplicit, initializer)
    }


    fun parseFieldDeclaration(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
        startPos: Int,
        local: Boolean,
        mutable: Boolean,
    ) : FieldDefinition {
        val name = tokenizer.consume(TokenType.IDENTIFIER)
        val kind = if (local) Definition.Kind.FIELD
            else Definition.Kind.STATIC
        val text = consumeLine(tokenizer, startPos)

        return FieldDefinition(context.scope, kind, name, definitionText = text)
    }



}