package org.kobjects.tantilla2.core.parser

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.F64
import org.kobjects.greenspun.core.Str
import org.kobjects.tantilla2.core.*
import org.kobjects.parserlib.expressionparser.ExpressionParser as GreenspunExpressionParser
import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.classifier.StructMetaType
import org.kobjects.tantilla2.core.function.FunctionDefinition
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.node.*

object ExpressionParser {

    fun parseExpression(tokenizer: TantillaTokenizer, context: ParsingContext, expectedType: Type? = null): Evaluable<RuntimeContext> {
        if (expectedType is FunctionType && tokenizer.current.text == "lambda") {
            return parseLambda(tokenizer, context, expectedType)
        }
        val result = expressionParser.parse(tokenizer, context)
        return matchType(context.scope, result, expectedType)
    }


    fun matchType(context: Scope, expr: Evaluable<RuntimeContext>, expectedType: Type?): Evaluable<RuntimeContext> {
        if (expectedType == null || expectedType.isAssignableFrom(expr.returnType)) {
            return expr
        }
        val implName = expectedType.typeName + " for " + expr.returnType.typeName
        try {
            val impl = context.resolveStatic(implName, true)!!.value as ImplDefinition
            return As(expr, impl, implicit = true)
        } catch (e: Exception) {
            throw IllegalArgumentException("Can't convert $expr with type '${expr.returnType}' to '$expectedType' -- '$implName' not available.", e)
        }
    }

    fun parseElementAt(tokenizer: TantillaTokenizer, context: ParsingContext, base: Evaluable<RuntimeContext>): Evaluable<RuntimeContext> {
        val result = ElementAt(base, parseExpression(tokenizer, context))
        tokenizer.consume("]")
        return result
    }

    fun reference(scope: Scope, definition: Definition, qualified: Boolean) = if (definition.kind == Definition.Kind.FIELD) {
        val depth = definition.depth(scope)
        LocalVariableReference(
            definition.name, definition.type, depth, definition.index, definition.mutable
        )
    }
    else StaticReference(definition, qualified)


    fun parseFreeIdentifier(tokenizer: TantillaTokenizer, context: ParsingContext): Evaluable<RuntimeContext> {
        val name = tokenizer.consume(TokenType.IDENTIFIER)
        val scope = context.scope

        val dynamicDefinition = scope.resolveDynamic(name, fallBackToStatic = false)
        if (dynamicDefinition != null && dynamicDefinition.isDynamic()) {
            return parseMaybeApply(
                tokenizer,
                context,
                reference(scope, dynamicDefinition, false),
                self = null,
                openingParenConsumed = false,
                asMethod = false
            )
        }

        val self = context.scope.resolveDynamic("self", fallBackToStatic = false)
        val selfType = self?.type
        if (selfType is Scope) {
            val definition = selfType.resolveDynamic(name, fallBackToStatic = false)
            if (definition != null) {
                return property(tokenizer, context, reference(scope, self, false), definition)
            }
        }

        val staticDefinition = scope.resolveStatic(name, fallBackToParent = true)
        if (staticDefinition != null) {
            return parseMaybeApply(
                tokenizer,
                context,
                reference(scope, staticDefinition, false),
                self = null,
                openingParenConsumed = false,
                asMethod = false)
        }

        if (tokenizer.tryConsume("(")) {
            val firstParameter = parseExpression(tokenizer, context)
            val baseType = firstParameter.returnType as Scope
            val definition = baseType.definitions[name]
            if (!tokenizer.tryConsume(",") && tokenizer.current.text != ")") {
                throw tokenizer.exception("Comma or closing paren expected after first parameter.")
            }
            if (definition != null) {
                return parseMaybeApply(
                    tokenizer,
                    context,
                    reference(context.scope, definition, false),
                    firstParameter,
                    openingParenConsumed = true,
                    asMethod = false
                )
            }
        }

        throw tokenizer.exception("Symbol not found: '$name'.")
    }

    // Add support for known signature later
    fun parseLambda(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
        expectedType: FunctionType? = null
    ): Evaluable<RuntimeContext> {
        tokenizer.consume("lambda")

        val type: FunctionType
        val parameterNames: List<String>
        if (tokenizer.current.text == "(") {
            type = TypeParser.parseFunctionType(tokenizer, context, isMethod = false)
            if (expectedType != null && !expectedType.isAssignableFrom(type)) {
                throw tokenizer.exception("Function type $type does not match expected type $expectedType")
            }
            parameterNames = type.parameters.map { it.name }
        } else {
            if (expectedType == null) {
                throw tokenizer.exception("For lambdas with unknown type, a full parameter list starting with '(' is expected.")
            }
            type = expectedType
            val names = mutableListOf<String>()
            if (tokenizer.current.text != ":") {
              do {
                  names.add(tokenizer.consume(TokenType.IDENTIFIER))
              } while (tokenizer.tryConsume(","))
            }
            if (names.size > type.parameters.size) {
                throw tokenizer.exception("${names.size} parameters provided, but only ${type.parameters.size} parameters expected (type: $type).")
            }
            while (names.size < type.parameters.size) {
                names.add("$${names.size}")
            }
            parameterNames = names.toList()
        }

        tokenizer.consume(":")
        val functionScope = FunctionDefinition(context.scope, Definition.Kind.FUNCTION, "<anonymous>", "", resolvedType = type)
        for (i in type.parameters.indices) {
            val parameter = type.parameters[i]
            functionScope.declareLocalVariable(parameterNames[i], parameter.type, false)
        }
/*
        val closureIndices = mutableListOf<Int>()
        for (definition in context.scope) {
            if (definition.kind == Definition.Kind.LOCAL_VARIABLE && !parameterNames.contains(definition.name)) {
                functionScope.declareLocalVariable(definition.name, definition.type(), definition.mutable)
                closureIndices.add(definition.index)
            }
        } */
        val body = Parser.parse(tokenizer, ParsingContext(functionScope, context.depth + 1))

        return LambdaReference(type, functionScope.definitions.locals.size, body)
    }


    fun parsePrimary(tokenizer: TantillaTokenizer, context: ParsingContext): Evaluable<RuntimeContext> =
        when (tokenizer.current.type) {
            TokenType.NUMBER -> F64.Const(tokenizer.next().text.toDouble())
            TokenType.STRING -> Str.Const(tokenizer.next().text.unquote())
            TokenType.IDENTIFIER ->  if (tokenizer.current.text == "lambda")
                parseLambda(tokenizer, context) else parseFreeIdentifier(tokenizer, context)
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
        context: ParsingContext,
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
        context: ParsingContext,
        base: Evaluable<RuntimeContext>,
    ): Evaluable<RuntimeContext> {
        val traitName = tokenizer.consume(TokenType.IDENTIFIER)
        val className = base.returnType.typeName
        val impl = context.scope.resolveStatic("$traitName for $className")!!.value as ImplDefinition
        impl.rebuild(CompilationResults())
        return As(base, impl, implicit = false)
    }

    fun parseProperty(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
        base: Evaluable<RuntimeContext>,
    ): Evaluable<RuntimeContext> {
        val name = tokenizer.consume(TokenType.IDENTIFIER)
        val baseType = base.returnType
        val definition = baseType.resolve(name)
            ?: throw tokenizer.exception("Property '$name' not found.")
        return property(tokenizer, context, base, definition)
    }

    fun property(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
        base: Evaluable<RuntimeContext>,
        definition: Definition
    ): Evaluable<RuntimeContext> {
        var self: Evaluable<RuntimeContext>? = null
        val name = definition.name
        val value = when (definition.kind) {
            Definition.Kind.FIELD ->
                PropertyReference(
                    base,
                    name,
                    definition.type,
                    definition.index,
                    definition.mutable
                )
            Definition.Kind.METHOD -> {
                self = base
                StaticReference(definition, false)
            }
            Definition.Kind.FUNCTION -> StaticReference(definition, true)
            Definition.Kind.STATIC -> StaticReference(definition, true)
            else -> throw tokenizer.exception("Unsupported definition kind ${definition.kind} for $base.$name")
        }
        return parseMaybeApply(
            tokenizer,
            context,
            value,
            self,
            openingParenConsumed = false,
            asMethod = self != null)
    }

    fun parseMaybeApply(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
        value: Evaluable<RuntimeContext>,
        self: Evaluable<RuntimeContext>?,
        openingParenConsumed: Boolean,
        asMethod: Boolean
    ): Evaluable<RuntimeContext> {
        val type = value.returnType

        if (type !is FunctionType) {
            // Not a function, just skip () and error otherwise

            if (openingParenConsumed || tokenizer.tryConsume("(")) {
                tokenizer.consume(")", "Empty parameter list expected.")
            }
            return value
        }

        val hasArgs = openingParenConsumed || tokenizer.tryConsume("(")
        if (!hasArgs && type is StructMetaType) {
            return value
        }

        val expectedParameters = type.parameters
        val parameterExpressions = MutableList<Evaluable<RuntimeContext>?>(expectedParameters.size) { null }
        var index = 0
        if (self != null) {
            parameterExpressions[index++] = self
        }

        val indexMap = mutableMapOf<String, Int>()
        for (i in expectedParameters.indices) {
            indexMap[expectedParameters[i].name] = i
        }

        val varargs = mutableListOf<Evaluable<RuntimeContext>>()
        var varargIndex = -1
        var nameRequired = false
        if (hasArgs && !tokenizer.tryConsume(")")) {
            do {
                if (tokenizer.current.type == TokenType.IDENTIFIER && tokenizer.lookAhead(1).text == "=") {
                    val name = tokenizer.consume(TokenType.IDENTIFIER)
                    tokenizer.consume("=")
                    nameRequired = true
                    index = indexMap[name] ?: throw tokenizer.exception("Parameter name '$name' not found.")
                } else if (nameRequired) {
                    throw tokenizer.exception("Named parameter required here.")
                } else if (index >= expectedParameters.size) {
                    throw tokenizer.exception("Too many parameters.")
                }
                val expectedParameter = expectedParameters[index]
                val expression = parseExpression(tokenizer, context, expectedParameter.type)
                if (expectedParameter.isVararg) {
                    varargs.add(expression)
                    varargIndex = index
                } else {
                    parameterExpressions[index++] = expression
                }
            } while (tokenizer.tryConsume(","))
            tokenizer.consume(")")
        }

        if (varargIndex != -1) {
            parameterExpressions[varargIndex] = ListLiteral(varargs)
        }

        for (i in expectedParameters.indices) {
            val expectedParameter = expectedParameters[i]
            if (parameterExpressions[i] == null) {
                if (expectedParameter.defaultValueExpression == null) {
                    throw tokenizer.exception("Parameter ${expectedParameter.name} is missing.")
                }
                parameterExpressions[i] = expectedParameter.defaultValueExpression
            }
        }

        return Apply(
            value,
            List(parameterExpressions.size) { parameterExpressions[it]!!},
            !hasArgs,
            asMethod
        )
    }


    val expressionParser =
        GreenspunExpressionParser<TantillaTokenizer, ParsingContext, Evaluable<RuntimeContext>>(
            GreenspunExpressionParser.suffix(9, ".") {
                tokenizer, context, _, base -> parseProperty(tokenizer, context, base) },
            GreenspunExpressionParser.suffix(8, "[") {
                tokenizer, context, _, base -> parseElementAt(tokenizer, context, base)
            },
            GreenspunExpressionParser.suffix(8, "(") {
                tokenizer, context, _, base -> parseMaybeApply(
                tokenizer, context, base, self =null, openingParenConsumed = true, asMethod = false) },
            GreenspunExpressionParser.suffix(7, "as") {
                tokenizer, context, _, base -> parseAs(tokenizer, context, base) },
            GreenspunExpressionParser.infix(6, "**") { _, _, _, l, r -> F64.Pow(l, r)},
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
        ) {
                t, c -> parsePrimary(t, c)
        }
}