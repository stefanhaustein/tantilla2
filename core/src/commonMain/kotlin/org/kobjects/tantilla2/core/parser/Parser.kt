package org.kobjects.tantilla2.core.parser

import org.kobjects.greenspun.core.*
import org.kobjects.parserlib.expressionparser.ExpressionParser
import org.kobjects.tantilla2.core.node.StaticReference
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.classifier.*
import org.kobjects.tantilla2.core.node.For
import org.kobjects.tantilla2.core.function.*
import org.kobjects.tantilla2.core.node.*
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
        if (tokenizer.tryConsume("if")) {
            parseIf(tokenizer, context, currentDepth)
        } else if (tokenizer.tryConsume("while")) {
            parseWhile(tokenizer, context, currentDepth)
        } else if (tokenizer.tryConsume("for")) {
            parseFor(tokenizer, context, currentDepth)
        } else {
            val expr = parseExpression(tokenizer, context)
            if (tokenizer.tryConsume("=")) {
                if (expr !is Assignable) {
                    tokenizer.exception("Target is not assignable")
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

    fun parseWhile(tokenizer: TantillaTokenizer, context: Scope, currentDepth: Int): Control.While<RuntimeContext> {
        val condition = parseExpression(tokenizer, context)
        tokenizer.consume(":")
        return Control.While(condition, parse(tokenizer, context, currentDepth))
    }

    fun parseFor(tokenizer: TantillaTokenizer, context: Scope, currentDepth: Int): For {
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

    fun parseExpression(tokenizer: TantillaTokenizer, context: Scope): Evaluable<RuntimeContext> =
        expressionParser.parse(tokenizer, context)

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
        val functionContext = FunctionScope(context)
        for (parameter in type.parameters) {
            functionContext.declareLocalVariable(parameter.name, parameter.type, false)
        }
        val body = parse(tokenizer, functionContext, 0)
        return LambdaImpl(type, functionContext.iterator().asSequence().toList().size, body)
    }

    fun matchType(context: Scope, expr: Evaluable<RuntimeContext>, expectedType: Type): Evaluable<RuntimeContext> {
        if (expectedType.isAssignableFrom(expr.returnType)) {
            return expr
        }
        val implName = expectedType.typeName + " for " + expr.returnType.typeName
        try {
            val impl = context.resolveStatic(implName, true).value() as ImplDefinition
            return As(expr, impl)
        } catch (e: Exception) {
            throw IllegalArgumentException("Can't convert $expr with type '${expr.returnType}' to '$expectedType' -- '$implName' not available.", e)
        }
    }


    fun parseParameter(tokenizer: TantillaTokenizer, context: Scope): Parameter {
        val name = tokenizer.consume(TokenType.IDENTIFIER)
        tokenizer.consume(":", "Colon expected, separating the parameter type from the parameter name.")
        val type = parseType(tokenizer, context)
        val defaultValue: Evaluable<RuntimeContext>?
        if (tokenizer.tryConsume("=")) {
            defaultValue = matchType(context, parseExpression(tokenizer, context), type)
        } else {
            defaultValue = null
        }
        return Parameter(name, type, defaultValue)
    }

    fun parseApply(tokenizer: TantillaTokenizer, context: Scope, base: Evaluable<RuntimeContext>): Evaluable<RuntimeContext> {
        return apply(context, base, parseParameterList(tokenizer, context))
    }

    fun reference(definition: Definition) = if (definition.kind == Definition.Kind.LOCAL_VARIABLE)
        LocalVariableReference(
            definition.name, definition.type(), definition.index, definition.mutable)
        else StaticReference(definition)

    fun apply(
        scope: Scope,
        base: Evaluable<RuntimeContext>,
        availableParameters: List<Pair<String, Evaluable<RuntimeContext>>>
    ): Evaluable<RuntimeContext> {
        val functionType = base.returnType as FunctionType

        val expectedParameters = functionType.parameters

        val result = mutableListOf<Evaluable<RuntimeContext>>()
        for (i in 0 until min(expectedParameters.size, availableParameters.size)) {
            if (availableParameters[i].first.isNotEmpty()) {
                break
            }
            result.add(matchType(scope, availableParameters[i].second, expectedParameters[i].type))
        }
        if (result.size < functionType.parameters.size) {
            val map = mutableMapOf<String, Evaluable<RuntimeContext>>()
            for (i in result.size until availableParameters.size) {
                val actual = availableParameters[i]
                map.put(actual.first, actual.second)
            }
            for (i in result.size until functionType.parameters.size) {
                val expected = expectedParameters[i]
                val name = expected.name
                val expr = map.remove(expected.name) ?: expected.defaultValueExpression
                ?: throw IllegalArgumentException("Parameter not found: '$name' expected: $expectedParameters provided: $availableParameters")
                result.add(matchType(scope, expr, expected.type))
            }
            if (map.isNotEmpty()) {
                throw IllegalArgumentException("Unexpected parameter(s): ${map.keys}")
            }
        }

        return Apply(base, result.toList())
    }

    fun parseFreeIdentifier(tokenizer: TantillaTokenizer, context: Scope): Evaluable<RuntimeContext> {
        val name = tokenizer.consume(TokenType.IDENTIFIER)

        var args: List<Pair<String, Evaluable<RuntimeContext>>>
        var hasArgs: Boolean
        if (tokenizer.tryConsume("(")) {
            hasArgs = true
            args = parseParameterList(tokenizer, context)
            if (args.size > 0 && args[0].first == "" && args[0].second.returnType is Scope) {
                val baseType = args[0].second.returnType as Scope
                val definition = baseType[name]
                if (definition != null) {
                    return apply(context, StaticReference(definition), args)
                }
            }
        } else {
            hasArgs = false
            args = emptyList()
        }

        val definition = context.resolveDynamic(name, fallBackToStatic = true)
        val base = reference(definition)
        if (base.returnType is FunctionType && (hasArgs || base.returnType !is UserClassMetaType)) {
            return apply(context, base, args)
        }
        if (args.size > 0) {
            throw IllegalArgumentException("Not callable: ${definition.scope.title}.${definition.name}")
        }
        return base
    }

    fun parseParameterList(tokenizer: TantillaTokenizer, context: Scope): List<Pair<String, Evaluable<RuntimeContext>>> {
        if (tokenizer.tryConsume(")")) {
            return emptyList()
        }
        val result = mutableListOf<Pair<String, Evaluable<RuntimeContext>>>()
        do {
            var name: String
            if (tokenizer.current.type == TokenType.IDENTIFIER && tokenizer.lookAhead(1).text == "=") {
                name = tokenizer.consume(TokenType.IDENTIFIER)
                tokenizer.consume("=")
            } else {
                name = ""
            }
            val expression = parseExpression(tokenizer, context)
            result.add(Pair(name, expression))
        } while (tokenizer.tryConsume(","))
        tokenizer.consume(")")
        return result.toList()
    }


    fun parsePrimary(tokenizer: TantillaTokenizer, context: Scope): Evaluable<RuntimeContext> =
        when (tokenizer.current.type) {
            TokenType.NUMBER -> F64.Const(tokenizer.next().text.toDouble())
            TokenType.STRING -> Str.Const(tokenizer.next().text.unquote())
            TokenType.IDENTIFIER -> parseFreeIdentifier(tokenizer, context)
            else -> {
                when (tokenizer.current.text) {
                    "(" -> {
                        tokenizer.consume("(")
                        val result = parseExpression(tokenizer, context)
                        tokenizer.consume(")")
                        result
                    }
                    "[" -> {
                        tokenizer.consume("[")
                        ListLiteral(parseList(tokenizer, context, "]"))
                    }
                    else -> throw tokenizer.exception("Number, identifier or opening bracket expected here.")
                }
            }
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

    fun parseList(
        tokenizer: TantillaTokenizer,
        context: Scope,
        endMarker: String): List<Evaluable<RuntimeContext>> {
        val result = mutableListOf<Evaluable<RuntimeContext>>()
        if (tokenizer.current.text != endMarker) {
            do {
                result.add(parseExpression(tokenizer, context))
            } while (tokenizer.tryConsume(","))
        }
        tokenizer.consume(endMarker, "$endMarker or , expected")
        return result.toList()
    }

    fun parseAs(
        tokenizer: TantillaTokenizer,
        context: Scope,
        base: Evaluable<RuntimeContext>,
    ): Evaluable<RuntimeContext> {
        val traitName = tokenizer.consume(TokenType.IDENTIFIER)
        val className = base.returnType.typeName
        val impl = context.resolveStatic("$traitName for $className").value() as ImplDefinition
        impl.hasError()
        return As(base, impl)
    }

    fun parseProperty(
        tokenizer: TantillaTokenizer,
        context: Scope,
        base: Evaluable<RuntimeContext>,
    ): Evaluable<RuntimeContext> {
        val name = tokenizer.consume(TokenType.IDENTIFIER)
        val baseType = base.returnType
        val definition = baseType.resolve(name)
        val args = if (tokenizer.tryConsume("(")) parseParameterList(tokenizer, context)
        else emptyList()

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
                val fn = StaticReference(definition)

                if (definition.isStatic()) {
                    return apply(context, fn, args)
                }

                val params = List<Pair<String, Evaluable<RuntimeContext>>>(args.size + 1) {
                    if (it == 0) Pair("", base) else args[it - 1]
                }
                return apply(context, fn, params)
            }
            Definition.Kind.STATIC_VARIABLE -> return StaticReference(definition)
            else -> throw tokenizer.exception("Unsupported definition kind ${definition.kind} for $base.$name")
        }
    }

    val expressionParser = ExpressionParser<TantillaTokenizer, Scope, Evaluable<RuntimeContext>>(
        ExpressionParser.suffix(8, "as") {
          tokenizer, context, _, base -> parseAs(tokenizer, context, base) },
        ExpressionParser.suffix(7, ".") {
                tokenizer, context, _, base -> parseProperty(tokenizer, context, base) },
        ExpressionParser.suffix(6, "(") {
                tokenizer, context, _, base -> parseApply(tokenizer, context, base) },
        ExpressionParser.infix(5, "*") { _, _, _, l, r -> F64.Mul(l, r)},
        ExpressionParser.infix(5, "/") { _, _, _, l, r -> F64.Div(l, r)},
        ExpressionParser.infix(5, "%") { _, _, _, l, r -> F64.Mod(l, r)},
        ExpressionParser.prefix(4, "-") {_, _, _, expr -> F64.Sub(F64.Const<RuntimeContext>(0.0), expr)},
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