package org.kobjects.tantilla2.core.parser

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.F64
import org.kobjects.greenspun.core.Str
import org.kobjects.tantilla2.core.*
import org.kobjects.parserlib.expressionparser.ExpressionParser as GreenspunExpressionParser
import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.classifier.UserClassMetaType
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.node.*
import org.kobjects.tantilla2.core.runtime.ListType
import kotlin.math.min

object ExpressionParser {

    fun parseExpression(tokenizer: TantillaTokenizer, context: Scope): Evaluable<RuntimeContext> =
        expressionParser.parse(tokenizer, context)




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



    fun parseApply(tokenizer: TantillaTokenizer, context: Scope, base: Evaluable<RuntimeContext>): Evaluable<RuntimeContext> {
        return apply(context, base, parseParameterList(tokenizer, context))
    }

    fun parseElementAt(tokenizer: TantillaTokenizer, scope: Scope, base: Evaluable<RuntimeContext>): Evaluable<RuntimeContext> {
        val result = ElementAt(base, parseExpression(tokenizer, scope))
        tokenizer.consume("]")
        return result
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






    val expressionParser = GreenspunExpressionParser<TantillaTokenizer, Scope, Evaluable<RuntimeContext>>(
        GreenspunExpressionParser.suffix(8, "as") {
                tokenizer, context, _, base -> parseAs(tokenizer, context, base) },
        GreenspunExpressionParser.suffix(7, ".") {
                tokenizer, context, _, base -> parseProperty(tokenizer, context, base) },
        GreenspunExpressionParser.suffix(6, "[") {
                tokenizer, context, _, base -> parseElementAt(tokenizer, context, base)
        },
        GreenspunExpressionParser.suffix(6, "(") {
                tokenizer, context, _, base -> parseApply(tokenizer, context, base) },
        GreenspunExpressionParser.infix(5, "*") { _, _, _, l, r -> F64.Mul(l, r)},
        GreenspunExpressionParser.infix(5, "/") { _, _, _, l, r -> F64.Div(l, r)},
        GreenspunExpressionParser.infix(5, "%") { _, _, _, l, r -> F64.Mod(l, r)},
        GreenspunExpressionParser.prefix(4, "-") { _, _, _, expr -> F64.Sub(F64.Const<RuntimeContext>(0.0), expr)},
        GreenspunExpressionParser.infix(3, "+") { _, _, _, l, r -> F64.Add(l, r)},
        GreenspunExpressionParser.infix(3, "-") { _, _, _, l, r -> F64.Sub(l, r)},
        GreenspunExpressionParser.infix(2, "==") { _, _, _, l, r -> F64.Eq(l, r)},
        GreenspunExpressionParser.infix(2, "!=") { _, _, _, l, r -> F64.Ne(l, r)},
        GreenspunExpressionParser.infix(1, "<") { _, _, _, l, r -> F64.Lt(l, r)},
        GreenspunExpressionParser.infix(1, ">") { _, _, _, l, r -> F64.Gt(l, r)},
        GreenspunExpressionParser.infix(1, "<=") { _, _, _, l, r -> F64.Le(l, r)},
        GreenspunExpressionParser.infix(1, ">=") { _, _, _, l, r -> F64.Ge(l, r)},

        ) { t, c -> parsePrimary(t, c)
    }
}