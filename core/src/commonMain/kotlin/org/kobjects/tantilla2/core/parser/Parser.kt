package org.kobjects.tantilla2.core.parser

import DefinitionParser
import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.parserlib.tokenizer.Token
import org.kobjects.tantilla2.core.definition.*
import org.kobjects.tantilla2.core.function.*
import org.kobjects.tantilla2.core.node.statement.BlockNode
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.node.statement.Comment
import org.kobjects.tantilla2.core.type.Type

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

    fun parseShellInput(s: String, localScope: LambdaScope, scope: UserRootScope): Node {
        return parse(s, statementScope = localScope, definitionScope = scope)
    }

    fun parseProgram(code: String, userRootScope: UserRootScope) {
        parse(code, statementScope = null, definitionScope = userRootScope)
    }

    private fun parse(
        source: String,
        statementScope: Scope? = null,
        definitionScope: Scope? = null,
    ): Node {
        val tokenizer = TantillaScanner(source)
        try {
            // scope.docString = if (statementsAllowed) "" else readDocString(tokenizer)
            val result = parseDefinitionsAndStatements(
                tokenizer,
                0,
                statementScope = statementScope,
                definitionScope = definitionScope
            )
            tokenizer.requireEof()
            return result
        } catch (e: Exception) {
            throw tokenizer.ensureParsingException(e)
        }
    }

    fun parseStatements(tokenizer: TantillaScanner, context: ParsingContext, errors: MutableList<ParsingException>? = null) =
        parseDefinitionsAndStatements(tokenizer, context.depth, statementScope = context.scope, definitionScope = null, errorCollector = errors)

    fun parseDefinitions(
        tokenizer: TantillaScanner,
        context: ParsingContext,
        definitionCallback: (Definition) -> Unit = { context.scope!!.add(it) },
        errorCollector: MutableList<ParsingException>? = null
    ) {
        parseDefinitionsAndStatements(tokenizer, context.depth, statementScope = null, definitionScope = context.scope, definitionCallback = definitionCallback, errorCollector = errorCollector)
    }

    /**
     * If the error return parameter is set, it's used to report errors and this function shouldn't
     * throw. If the error return parameter is null, an exception is thrown when the first
     * error is encountered.
     */
    fun parseDefinitionsAndStatements(
        tokenizer: TantillaScanner,
        depth: Int,
        statementScope: Scope?,
        definitionScope: Scope?,
        definitionCallback: (Definition) -> Unit = { definitionScope!!.add(it) },
        errorCollector: MutableList<ParsingException>? = null,
    ): Node {
        require (statementScope != null || definitionScope != null)
        val statements = mutableListOf<Node>()
        var localDepth = depth

        if (statementScope == null && definitionScope is DocStringUpdatable) {
            definitionScope.docString = readDocString(tokenizer)
        }

        while (tokenizer.current.type != TokenType.EOF
            && !VALID_AFTER_STATEMENT.contains(tokenizer.current.text)
        ) {
            if (tokenizer.current.type == TokenType.LINE_BREAK) {
                localDepth = getIndent(tokenizer.current.text)

                // println("line break with depth $localDepth")
                if (localDepth < depth) {
                    break
                }
                if (statementScope != null) {
                    for (i in 1 until tokenizer.current.text.count{ it == '\n'}) {
                        statements.add(Comment(null))
                    }
                }
                tokenizer.consume()
            } else if (tokenizer.current.type == TokenType.DISABLED_CODE) {
                if (definitionScope == null) {
                    throw tokenizer.exception("Definitions are not allowed here.")
                }
                val code = tokenizer.current.text
                definitionCallback(DefinitionParser.parseFailsafe(definitionScope, code.substring(4, code.length - 4)))
                tokenizer.consume()
            } else if (definitionScope != null && (DECLARATION_KEYWORDS.contains(tokenizer.current.text)
                        || statementScope == null)) {
                val definition = DefinitionParser.parseDefinitionFailsafe(tokenizer, ParsingContext(definitionScope, localDepth), errorCollector)
                definitionCallback(definition)
            } else {
                val statement = StatementParser.parseStatementFailsafe(tokenizer, ParsingContext(statementScope!!, localDepth), errorCollector)
                statements.add(statement)
            }
        }
        return if (statements.size == 1) statements[0]
            else BlockNode(*statements.toTypedArray())
    }

    fun readDocString(tokenizer: TantillaScanner): String {
        if (tokenizer.current.type == TokenType.STRING || tokenizer.current.type == TokenType.MULTILINE_STRING) {
            return tokenizer.consume().text.unquote()
        }
        return ""
    }


    fun consumeInBrackets(tokenizer: TantillaScanner) {
        val end: String
        when(tokenizer.current.text) {
            "=" -> {
                tokenizer.consume()
                while(tokenizer.current.type == TokenType.LINE_BREAK) {
                    tokenizer.consume()
                }
                return
            }
            "(" -> end = ")"
            "[" -> end = "]"
            "{" -> end = "}"
            else -> return
        }
        do {
            tokenizer.consume()
        } while (tokenizer.current.text != end
            && tokenizer.current.type != TokenType.EOF)
    }

    fun consumeLine(tokenizer: TantillaScanner, startPos: Token<TokenType>): CodeFragment {
        while (tokenizer.current.type != TokenType.EOF && tokenizer.current.type != TokenType.LINE_BREAK) {
            consumeInBrackets(tokenizer)
            tokenizer.consume()
        }
        return CodeFragment(startPos, tokenizer.code.substring(startPos.localPos, tokenizer.current.localPos))
    }

    fun consumeBody(
        tokenizer: TantillaScanner,
        startPos: Token<TokenType>,
        returnDepth: Int
    ): CodeFragment {
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
                return CodeFragment(startPos, tokenizer.code.substring(startPos.localPos, tokenizer.current.localPos))
            }
            tokenizer.consume()
        }
        return CodeFragment(startPos, tokenizer.code.substring(startPos.localPos))
    }

    fun skipLineBreaks(tokenizer: TantillaScanner, currentDepth: Int) {
        while (tokenizer.current.type == TokenType.LINE_BREAK
            && getIndent(tokenizer.current.text) >= currentDepth) {
            tokenizer.consume()
        }
    }


    fun resolveVariable(tokenizer: TantillaScanner, context: ParsingContext, typeOnly: Boolean = false):
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
            while (tokenizer.current.type == TokenType.LINE_BREAK) {
                tokenizer.consume()
            }
            initializer = TantillaExpressionParser.parseExpression(tokenizer, context)
            if (type == null) {
                type = initializer.returnType
            } else {
                initializer = TantillaExpressionParser.matchType(initializer, type)
            }
        } else if (type == null) {
            throw tokenizer.exception("Explicit type or initializer expression required.")
        }
        return Triple(type, typeIsExplicit, initializer)
    }


}