package org.kobjects.tantilla2.core.parser

import org.kobjects.greenspun.core.*
import org.kobjects.tantilla2.core.node.StaticReference
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.classifier.*
import org.kobjects.tantilla2.core.node.For
import org.kobjects.tantilla2.core.function.*
import org.kobjects.tantilla2.core.node.*
import org.kobjects.tantilla2.core.parser.ExpressionParser.parseExpression
import org.kobjects.tantilla2.core.runtime.ListType
import org.kobjects.tantilla2.core.runtime.RangeType
import org.kobjects.tantilla2.core.runtime.Void
import kotlin.math.min


fun String.unquote() = this.substring(1, this.length - 1)

object Parser {
    val DECLARATION_KEYWORDS = setOf("def", "var", "val", "class", "trait", "impl", "static")

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
        val result = parse(tokenizer, context)
        tokenizer.consume(TokenType.EOF)
        return result
    }

    fun parse(
        tokenizer: TantillaTokenizer,
        context: Scope,
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
            } else if (DECLARATION_KEYWORDS.contains(tokenizer.current.text)) {
                    val definition = parseDefinition(tokenizer, context, localDepth)
                    context.add(definition)
                    if (definition.kind == Definition.Kind.LOCAL_VARIABLE) {
                        if (context !is FunctionScope && context !is UserClassDefinition) {
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
                    val statement = parseStatement(tokenizer, context, localDepth)
                    println("parsed statement: $statement")
                    statements.add(statement)

            }
        }
        return if (statements.size == 1) statements[0]
            else Control.Block<RuntimeContext>(*statements.toTypedArray())
    }

    fun parseDefinition(tokenizer: TantillaTokenizer, scope: Scope, localDepth: Int) =
        when (tokenizer.current.text) {
            "static", "var", "val" -> parseVariableDeclaration(tokenizer, scope)
            "def" -> {
                tokenizer.consume("def")
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Function name expected.")
                println("consuming def $name")
                val text = consumeBody(tokenizer, localDepth)
                Definition(scope, Definition.Kind.FUNCTION, name, definitionText = text)
            }
            "class" -> {
                tokenizer.consume("class")
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Class name expected.")
                println("consuming class $name; return depth: $localDepth")
                val text = consumeBody(tokenizer, localDepth)
                Definition(scope, Definition.Kind.CLASS, name, definitionText = text)
            }
            "trait" -> {
                tokenizer.consume("trait")
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Trait name expected.")
                println("consuming trait $name; return depth: $localDepth")
                val text = consumeBody(tokenizer, localDepth)
                Definition(scope, Definition.Kind.TRAIT, name, definitionText = text)
            }
            "impl" -> {
                tokenizer.consume("impl")
                val traitName = tokenizer.consume(TokenType.IDENTIFIER, "Trait name expected.")
                tokenizer.consume("for")
                val name = tokenizer.consume(TokenType.IDENTIFIER, "Class name expected.")
                println("consuming impl $traitName for $name; return depth: $localDepth")
                val text = consumeBody(tokenizer, localDepth)
                Definition(
                    scope,
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
        context: Scope,
        currentDepth: Int
    ) : Evaluable<RuntimeContext> =
        when (tokenizer.current.text) {
            "if" -> parseIf(tokenizer, context, currentDepth)
            "while" -> parseWhile(tokenizer, context, currentDepth)
            "for" -> parseFor(tokenizer, context, currentDepth)
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

    fun parseReturn(tokenizer: TantillaTokenizer, scope: Scope): Evaluable<RuntimeContext> {
        tokenizer.consume("return")
        if (scope !is FunctionScope) {
            throw tokenizer.exception("Function scope expected for 'return'")
        }
        if (scope.functionType.returnType == Void) {
            return FlowControl(Control.FlowSignal.Kind.RETURN)
        }
        val expression = parseExpression(tokenizer, scope)
        return FlowControl(Control.FlowSignal.Kind.RETURN, expression)
    }

    fun parseWhile(tokenizer: TantillaTokenizer, context: Scope, currentDepth: Int): Control.While<RuntimeContext> {
        tokenizer.consume("while")
        val condition = parseExpression(tokenizer, context)
        tokenizer.consume(":")
        return Control.While(condition, parse(tokenizer, context, currentDepth))
    }

    fun parseFor(tokenizer: TantillaTokenizer, context: Scope, currentDepth: Int): For {
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

        val iteratorIndex = context.declareLocalVariable(
            iteratorName, iteratorType, false)
        val body = parse(tokenizer, context, currentDepth)
        return For(iteratorName, iteratorIndex, iterableExpression, body)
    }

    fun parseIf(tokenizer: TantillaTokenizer, context: Scope, currentDepth: Int): Control.If<RuntimeContext> {
        tokenizer.consume("if")
        val expressions = mutableListOf<Evaluable<RuntimeContext>>()
        println("parsing if at depth $currentDepth")
        do {
            val condition = ExpressionParser.parseExpression(tokenizer, context)
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

    fun parseVariableDeclaration(
        tokenizer: TantillaTokenizer,
        context: Scope,
    ) : Definition {
        val explicitlyStatic = tokenizer.tryConsume("static")
        val mutable = if (tokenizer.tryConsume("var")) true
            else if (tokenizer.tryConsume("val")) false
            else throw tokenizer.exception("var or val expected.")

        val name = tokenizer.consume(TokenType.IDENTIFIER)
        val kind = if (explicitlyStatic || (context !is UserClassDefinition && context !is FunctionScope)) Definition.Kind.STATIC_VARIABLE
            else Definition.Kind.LOCAL_VARIABLE
        val text = consumeLine(tokenizer)

        return Definition(context, kind, name, definitionText = text)
    }


    fun parseType(tokenizer: TantillaTokenizer, context: Scope): Type {
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
        return context.resolveStatic(name, true).value() as Type
    }


    fun parseParameter(tokenizer: TantillaTokenizer, context: Scope): Parameter {
        val isVararg = tokenizer.tryConsume("*")
        val name = tokenizer.consume(TokenType.IDENTIFIER)
        tokenizer.consume(":", "Colon expected, separating the parameter type from the parameter name.")
        val rawType = parseType(tokenizer, context)
        val type = if (isVararg) ListType(rawType) else rawType
        val defaultValue: Evaluable<RuntimeContext>? = if (tokenizer.tryConsume("="))
            ExpressionParser.matchType(context, parseExpression(tokenizer, context), type)
            else null

        return Parameter(name, type, defaultValue, isVararg)
    }

    fun parseFunctionType(tokenizer: TantillaTokenizer, context: Scope): FunctionType {
        tokenizer.consume("(")
        val parameters = mutableListOf<Parameter>()
        if (!tokenizer.tryConsume(")")) {
            if (tokenizer.tryConsume("self")) {
                val selfType: Type = when (context) {
                    is UserClassDefinition -> context
                        is TraitDefinition -> context
                    is ImplDefinition -> context.classifier
                    else ->
                    throw IllegalStateException("self supported for classes, traits and implemenetations only; got: ${context}")
                }
                parameters.add(Parameter("self", selfType, null))
                while (tokenizer.tryConsume(",")) {
                    parameters.add(parseParameter(tokenizer, context))
                }
            } else {
                do {
                    parameters.add(parseParameter(tokenizer, context))
                } while (tokenizer.tryConsume(","))
            }
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

    fun parseLambda(tokenizer: TantillaTokenizer, context: Scope): Lambda {
        val type = parseFunctionType(tokenizer, context)
        if (context is TraitDefinition) {
            tokenizer.consume(TokenType.EOF, "Trait methods must not have function bodies.")
            return TraitMethod(type, context.traitIndex++)
        }

        tokenizer.consume(":")
        val functionContext = FunctionScope(context, type)
        for (parameter in type.parameters) {
            functionContext.declareLocalVariable(parameter.name, parameter.type, false)
        }
        val body = parse(tokenizer, functionContext, 0)
        return LambdaImpl(type, functionContext.iterator().asSequence().toList().size, body)
    }



}