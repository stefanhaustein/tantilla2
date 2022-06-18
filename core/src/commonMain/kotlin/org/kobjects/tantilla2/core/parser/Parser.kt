package org.kobjects.tantilla2.core.parser

import org.kobjects.greenspun.core.*
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.classifier.*
import org.kobjects.tantilla2.core.node.For
import org.kobjects.tantilla2.core.function.*
import org.kobjects.tantilla2.core.node.*
import org.kobjects.tantilla2.core.parser.ExpressionParser.parseExpression
import org.kobjects.tantilla2.core.runtime.ListType
import org.kobjects.tantilla2.core.runtime.RangeType
import org.kobjects.tantilla2.core.runtime.Void


fun String.unquote() = this.substring(1, this.length - 1)

object Parser {
    val DECLARATION_KEYWORDS = setOf("def", "var", "val", "struct", "trait", "impl", "static")

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
        while (tokenizer.current.type != TokenType.EOF && tokenizer.current.text != "<|") {
            if (tokenizer.current.type == TokenType.LINE_BREAK) {
                localDepth = getIndent(tokenizer.current.text)
                println("line break with depth $localDepth")
                if (localDepth < context.depth) {
                    break
                }
                tokenizer.next()
            } else if (DECLARATION_KEYWORDS.contains(tokenizer.current.text)) {
                    val definition = parseDefinition(tokenizer, ParsingContext(scope, localDepth))
                    scope.add(definition)
                    if (definition.kind == Definition.Kind.LOCAL_VARIABLE) {
                        if (scope !is FunctionScope && scope !is UserClassDefinition) {
                            throw IllegalStateException()
                        }
                        if (definition.initializer() != null) {
                            statements.add(
                                Assignment(
                                    LocalVariableReference(
                                        definition.name,
                                        definition.type(),
                                        definition.index,
                                        definition.mutable
                                    ),
                                    definition.initializer()!!
                                )
                            )
                        }
                    }
                } else {
                    val statement = parseStatement(tokenizer, ParsingContext(scope, localDepth))
                    println("parsed statement: $statement")
                    statements.add(statement)

            }
        }
        return if (statements.size == 1) statements[0]
            else Control.Block<RuntimeContext>(*statements.toTypedArray())
    }

    fun parseDefinition(tokenizer: TantillaTokenizer, context: ParsingContext) =
        when (tokenizer.current.text) {
            "static", "var", "val" -> parseVariableDeclaration(tokenizer, context)
            "def" -> {
                tokenizer.consume("def")
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Function name expected.")
                println("consuming def $name")
                val text = consumeBody(tokenizer, context.depth)
                Definition(context.scope, Definition.Kind.FUNCTION, name, definitionText = text)
            }
            "struct" -> {
                tokenizer.consume("struct")
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Struct name expected.")
                val text = consumeBody(tokenizer, context.depth)
                Definition(context.scope, Definition.Kind.STRUCT, name, definitionText = text)
            }
            "trait" -> {
                tokenizer.consume("trait")
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Trait name expected.")
                val text = consumeBody(tokenizer, context.depth)
                Definition(context.scope, Definition.Kind.TRAIT, name, definitionText = text)
            }
            "impl" -> {
                tokenizer.consume("impl")
                val traitName = tokenizer.consume(TokenType.IDENTIFIER, "Trait name expected.")
                tokenizer.consume("for")
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Class name expected.")
                val text = consumeBody(tokenizer, context.depth)
                Definition(
                    context.scope,
                    Definition.Kind.IMPL,
                    "$traitName for $name",
                    definitionText = text
                )
            }
            else -> throw tokenizer.exception("Declaration expected.")
        }

    fun consumeLine(tokenizer: TantillaTokenizer): String {
        val pos = tokenizer.current.pos
        while (tokenizer.current.type != TokenType.EOF && tokenizer.current.type != TokenType.LINE_BREAK) {
            tokenizer.next()
        }
        return tokenizer.input.substring(pos, tokenizer.current.pos)
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
                    "def", "if", "while", "struct", "impl", "trait" -> localDepth++
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
    ) : Evaluable<RuntimeContext> =
        when (tokenizer.current.text) {
            "if" -> parseIf(tokenizer, context)
            "while" -> parseWhile(tokenizer, context)
            "for" -> parseFor(tokenizer, context)
            "return" -> parseReturn(tokenizer, context)
            else -> {
                var expr = parseExpression(tokenizer, context)
                if (tokenizer.tryConsume("=")) {
                    if (expr !is Assignable) {
                        tokenizer.exception("Target is not assignable")
                    }
                    expr = Assignment(expr as Assignable, ExpressionParser.parseExpression(tokenizer, context))
                }
                if (tokenizer.current.type != TokenType.EOF
                    && tokenizer.current.type != TokenType.LINE_BREAK
                    && tokenizer.current.text != "<|") {
                    throw tokenizer.exception("Unexpected token ${tokenizer.current} after end of statement.")
                }
                expr
            }
        }

    fun skipLineBreaks(tokenizer: TantillaTokenizer, currentDepth: Int) {
        while (tokenizer.current.type == TokenType.LINE_BREAK
            && getIndent(tokenizer.current.text) >= currentDepth) {
            tokenizer.next()
        }
    }

    fun parseReturn(tokenizer: TantillaTokenizer, context: ParsingContext): Evaluable<RuntimeContext> {
        tokenizer.consume("return")
        if (context.scope !is FunctionScope) {
            throw tokenizer.exception("Function scope expected for 'return'")
        }
        if (context.scope.functionType.returnType == Void) {
            return FlowControl(Control.FlowSignal.Kind.RETURN)
        }
        val expression = parseExpression(tokenizer, context)
        return FlowControl(Control.FlowSignal.Kind.RETURN, expression)
    }

    fun parseWhile(tokenizer: TantillaTokenizer, context: ParsingContext): Control.While<RuntimeContext> {
        tokenizer.consume("while")
        val condition = parseExpression(tokenizer, context)
        tokenizer.consume(":")
        return Control.While(condition, parse(tokenizer, context.indent()))
    }

    fun parseFor(tokenizer: TantillaTokenizer, context: ParsingContext): For {
        tokenizer.consume("for")
        val iteratorName = tokenizer.consume(TokenType.IDENTIFIER, "Loop variable name expected.")
        tokenizer.consume("in")
        val iterableExpression = parseExpression(tokenizer, context)
        tokenizer.consume(":")
        val iterableType = iterableExpression.returnType
        val iteratorType = when (iterableType) {
            RangeType -> org.kobjects.tantilla2.core.runtime.F64
            is ListType -> iterableType.elementType
            else -> throw RuntimeException("Can't iterate type $iterableType")
        }

        val iteratorIndex = context.scope.declareLocalVariable(
            iteratorName, iteratorType, false)
        val body = parse(tokenizer, context.indent())
        return For(iteratorName, iteratorIndex, iterableExpression, body)
    }

    fun parseIf(tokenizer: TantillaTokenizer, context: ParsingContext): Control.If<RuntimeContext> {
        tokenizer.consume("if")
        val expressions = mutableListOf<Evaluable<RuntimeContext>>()
        do {
            val condition = ExpressionParser.parseExpression(tokenizer, context)
            expressions.add(condition)
            tokenizer.consume(":")
            val block = parse(tokenizer, context.indent())
            expressions.add(block)
            skipLineBreaks(tokenizer, context.depth)
        } while (tokenizer.tryConsume("elif"))

        if (tokenizer.tryConsume("else")) {
            tokenizer.consume(":")
            val otherwise = parse(tokenizer, context.indent())
            expressions.add(otherwise)
        }

        return Control.If(*expressions.toTypedArray())
    }

    fun parseVariableDeclaration(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
    ) : Definition {
        val explicitlyStatic = tokenizer.tryConsume("static")
        val mutable = if (tokenizer.tryConsume("var")) true
            else if (tokenizer.tryConsume("val")) false
            else throw tokenizer.exception("var or val expected.")

        val name = tokenizer.consume(TokenType.IDENTIFIER)
        val kind = if (explicitlyStatic || (context.scope !is UserClassDefinition && context.scope !is FunctionScope)) Definition.Kind.STATIC_VARIABLE
            else Definition.Kind.LOCAL_VARIABLE
        val text = consumeLine(tokenizer)

        return Definition(context.scope, kind, name, definitionText = text)
    }


    fun parseDef(tokenizer: TantillaTokenizer, context: ParsingContext): Lambda {
        val type = TypeParser.parseFunctionType(tokenizer, context)
        if (context.scope is TraitDefinition) {
            tokenizer.consume(TokenType.EOF, "Trait methods must not have function bodies.")
            return TraitMethod(type, context.scope.traitIndex++)
        }

        tokenizer.consume(":")
        val functionScope = FunctionScope(context.scope, type)
        for (parameter in type.parameters) {
            functionScope.declareLocalVariable(parameter.name, parameter.type, false)
        }
        val body = parse(tokenizer, ParsingContext(functionScope, context.depth + 1))
        return LambdaImpl(type, functionScope.locals.size, body)
    }



}