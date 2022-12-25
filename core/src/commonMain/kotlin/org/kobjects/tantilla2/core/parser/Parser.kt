package org.kobjects.tantilla2.core.parser

import org.kobjects.tantilla2.core.classifier.*
import org.kobjects.tantilla2.core.definition.*
import org.kobjects.tantilla2.core.function.*
import org.kobjects.tantilla2.core.node.statement.BlockNode
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.Type


fun String.unquote() = this.substring(1, this.length - 1)

fun String.unquoteMultiline() = this.substring(3, this.length - 3)

fun String.unescape(): String {
    val sb = StringBuilder()
    var escaped = false
    for (c in this) {
        if (escaped) {
            when (c) {
                'n' -> sb.append('\n')
                'r' -> sb.append('\r')
                't' -> sb.append('\t')
                '\\' -> sb.append('\\')
                else -> throw IllegalArgumentException("Unsupported escape: $c")
            }
            escaped = false
        } else if (c == '\\') {
            escaped = true
        } else {
            sb.append(c)
        }
    }
    if (escaped) {
        throw IllegalArgumentException("Unterminated escape sequence.")
    }
    return sb.toString()
}

object Parser {
    val DECLARATION_KEYWORDS = setOf("def", "import", "struct", "trait", "unit", "impl", "static", "mut", "enum")

    val VALID_AFTER_STATEMENT = setOf(")", ",", "]", "}", "<|")

    fun getIndent(s: String): Int {
        val lastBreak = s.lastIndexOf('\n')
        if (lastBreak == -1) {
            throw IllegalArgumentException("Line break expected")
        }
        return s.length - lastBreak - 1
    }

    fun parseShellInput(s: String, scope: UserRootScope): Node {
        return parse(s, scope, definitionsAllowed = true, statementsAllowed = true)
    }

    fun parseProgram(s: String, scope: UserRootScope) {
        parse(s, scope, definitionsAllowed = true, statementsAllowed = false)
    }

    fun parse(
        source: String,
        scope: Scope,
        definitionsAllowed: Boolean = true,
        statementsAllowed: Boolean = true
    ): Node {
        val tokenizer = TantillaTokenizer(source)
        tokenizer.consume(TokenType.BOF)
        scope.docString = if (statementsAllowed) "" else readDocString(tokenizer)
        val result = parseDefinitionsAndStatements(
            tokenizer,
            ParsingContext(scope, 0),
            definitionsAllowed = definitionsAllowed,
            statementsAllowed = statementsAllowed
        )
        tokenizer.consume(TokenType.EOF)
        return result
    }

    fun parseStatements(tokenizer: TantillaTokenizer, context: ParsingContext) =
        parseDefinitionsAndStatements(tokenizer, context, definitionsAllowed = false)

    fun parseDefinitions(tokenizer: TantillaTokenizer, context: ParsingContext) {
        parseDefinitionsAndStatements(tokenizer, context, statementsAllowed = false)
    }

    fun parseDefinitionsAndStatements(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
        statementsAllowed: Boolean = true,
        definitionsAllowed: Boolean = true
    ): Node {
        val statements = mutableListOf<Node>()
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
            } else if (tokenizer.current.type == TokenType.DISABLED_CODE) {
                if (!definitionsAllowed) {
                    throw tokenizer.exception("Definitions are not allowed here.")
                }
                val code = tokenizer.current.text
                scope.add(parseFailsafe(scope, code.substring(4, code.length - 4)))
                tokenizer.next()
            } else if (DECLARATION_KEYWORDS.contains(tokenizer.current.text) ||
                (!statementsAllowed
                        && tokenizer.current.type == TokenType.IDENTIFIER
                        && (tokenizer.lookAhead(1).text == ":" || tokenizer.lookAhead(1).text == "="))) {
                if (!definitionsAllowed) {
                    throw tokenizer.exception("Definitions are not allowed here.")
                }
                val definition = parseDefinition(tokenizer, ParsingContext(scope, localDepth))
                scope.add(definition)
            } else if (statementsAllowed) {
                val statement = StatementParser.parseStatement(tokenizer, ParsingContext(scope, localDepth))
                statements.add(statement)
            } else {
                throw tokenizer.exception("Statements are not allowed here.")
            }
        }
        return if (statements.size == 1) statements[0]
            else BlockNode(*statements.toTypedArray())
    }

    fun readDocString(tokenizer: TantillaTokenizer): String {
        if (tokenizer.current.type == TokenType.STRING || tokenizer.current.type == TokenType.MULTILINE_STRING) {
            return unquote(tokenizer.next().text)
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
            "enum" -> parseEnum(tokenizer, context)
            "def" -> {
                val isMethod = !explicitlyStatic
                        && (scope is StructDefinition || scope is TraitDefinition || scope is ImplDefinition)
                tokenizer.consume("def")
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Function name expected.")
                println("consuming def $name")
                val text = consumeBody(tokenizer, startPos, context.depth)
                FunctionDefinition(context.scope, if (isMethod) Definition.Kind.METHOD else Definition.Kind.FUNCTION, name, definitionText = text)
            }
            "import" -> {
                tokenizer.consume(kind)
                val path = mutableListOf<String>()
                do {
                    path.add(tokenizer.consume(TokenType.IDENTIFIER))
                } while (tokenizer.tryConsume("."))
                ImportDefinition(context.scope, path)
            }
            "unit",
            "struct",
            "trait"-> {
                tokenizer.consume(kind)
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Struct name expected.")
                tokenizer.consume(":")
                val docString = readDocString(tokenizer)
                val definition = if (kind == "struct") StructDefinition(context.scope, name, docString = docString)
                else if (kind == "unit") UnitDefinition(context.scope, name, docString = docString)
                else TraitDefinition(context.scope, name, docString = docString)
                parseDefinitions(tokenizer, ParsingContext(definition, context.depth + 1))
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

    fun parseEnum(tokenizer: TantillaTokenizer, parsingContext: ParsingContext): EnumDefinition {
        tokenizer.consume("enum")
        val name = tokenizer.consume(TokenType.IDENTIFIER)
        val enumDefinition = EnumDefinition(parsingContext.scope, name)

        tokenizer.consume(":")
        while (tokenizer.current.type == TokenType.LINE_BREAK && getIndent(tokenizer.current.text) > parsingContext.depth) {
            tokenizer.consume(TokenType.LINE_BREAK)
            val literalName = tokenizer.consume(TokenType.IDENTIFIER)
            enumDefinition.add(EnumLiteral(enumDefinition, literalName))
        }
        return enumDefinition
    }

    fun consumeInBrackets(tokenizer: TantillaTokenizer) {
        val end: String
        when(tokenizer.current.text) {
            "=" -> {
                tokenizer.next()
                while(tokenizer.current.type == TokenType.LINE_BREAK) {
                    tokenizer.next()
                }
                return
            }
            "(" -> end = ")"
            "[" -> end = "]"
            "{" -> end = "}"
            else -> return
        }
        do {
            tokenizer.next()
        } while (tokenizer.current.text != end)
    }

    fun consumeLine(tokenizer: TantillaTokenizer, startPos: Int): String {
        while (tokenizer.current.type != TokenType.EOF && tokenizer.current.type != TokenType.LINE_BREAK) {
            consumeInBrackets(tokenizer)
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
        while (tokenizer.current.type != TokenType.EOF) {
            if (tokenizer.current.type == TokenType.LINE_BREAK) {
                localDepth = getIndent(tokenizer.current.text)
           //     println("- new local depth: $localDepth")
            } else {
                when (tokenizer.current.text) {
                    "def", "if", "while", "struct", "impl", "trait" -> localDepth++
                    "<|" -> localDepth--
                    else -> consumeInBrackets(tokenizer)
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
            Triple<Type, Boolean, Node?> {

        val scope = context.scope
        var type: Type? = null
        var initializer: Node? = null
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
        val kind = if (local) Definition.Kind.PROPERTY
            else Definition.Kind.STATIC
        val text = consumeLine(tokenizer, startPos)

        return FieldDefinition(context.scope, kind, name, definitionText = text)
    }

    fun parseFailsafe(parentScope: Scope, code: String): Definition {
        val tokenizer = TantillaTokenizer(code)
        tokenizer.next()
        var result: Definition
        try {
            result = parseDefinition(tokenizer, ParsingContext(parentScope, -1))

            while (tokenizer.current.type == TokenType.LINE_BREAK) {
                tokenizer.next()
            }

            if (tokenizer.current.type != TokenType.EOF) {
                result = UnparseableDefinition(parentScope, result.name, code)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            //            val name = oldDefinition?.name ?: "[error]"
            result = UnparseableDefinition(
                parentScope,
                definitionText = code
            )
        }
        return result
    }

}