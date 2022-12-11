package org.kobjects.tantilla2.core.parser

import org.kobjects.tantilla2.core.node.expression.StrNode
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.type.GenericType
import org.kobjects.tantilla2.core.type.StrType
import org.kobjects.parserlib.expressionparser.ExpressionParser as GreenspunExpressionParser
import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.classifier.StructMetaType
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.LambdaScope
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.node.expression.*
import org.kobjects.tantilla2.core.node.expression.Apply
import org.kobjects.tantilla2.core.type.FloatType
import org.kobjects.tantilla2.core.type.Type

object ExpressionParser {

    fun bothInt(l: Node, r: Node) =
         l.returnType == org.kobjects.tantilla2.core.type.IntType
                 && r.returnType == org.kobjects.tantilla2.core.type.IntType

    fun bothNumber(l: Node, r: Node) =
        FloatType.isAssignableFrom(l.returnType)
                && FloatType.isAssignableFrom(r.returnType)

    fun parseExpression(tokenizer: TantillaTokenizer, context: ParsingContext, expectedType: Type? = null): Node {
        if (expectedType is FunctionType && tokenizer.current.text == "lambda") {
            return parseLambda(tokenizer, context, expectedType)
        }
        val result = expressionParser.parse(tokenizer, context)
        return matchType(context.scope, result, expectedType)
    }


    fun matchType(context: Scope, expr: Node, expectedType: Type?): Node {
        if (expectedType == null || expectedType.isAssignableFrom(expr.returnType)) {
            return expr
        }
        val implName = expectedType.typeName + " for " + expr.returnType.typeName
        try {
            val impl = context.resolveStaticOrError(implName, true).getValue(null) as ImplDefinition
            return As(expr, impl, implicit = true)
        } catch (e: Exception) {
            throw IllegalArgumentException("Can't convert $expr with type '${expr.returnType}' to '$expectedType' -- '$implName' not available.", e)
        }
    }

    fun parseElementAt(tokenizer: TantillaTokenizer, context: ParsingContext, base: Node): Node {
        if (base.returnType is StructMetaType
            && (base.returnType as StructMetaType).wrapped is GenericType
        ) {
            val generic = ((base.returnType as StructMetaType).wrapped) as GenericType
            val typeParameters = mutableListOf<Type>()
            do {
                typeParameters.add(TypeParser.parseType(tokenizer, context))
            } while(tokenizer.tryConsume(","))
            tokenizer.consume("]")
            return GenericTypeNode(base, typeParameters)
        }

        val result = ElementAt(base, parseExpression(tokenizer, context))
        tokenizer.consume("]")
        return result
    }

    fun reference(scope: Scope, definition: Definition, qualified: Boolean) = if (definition.kind == Definition.Kind.PROPERTY) {
        val depth = definition.depth(scope)
        LocalVariableReference(
            definition.name, definition.type, depth, definition.index, definition.mutable
        )
    }
    else StaticReference(definition, qualified)


    fun parseFreeIdentifier(tokenizer: TantillaTokenizer, context: ParsingContext): Node {
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
            val definition = baseType[name]
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
    ): Node {
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
        val functionScope = LambdaScope(context.scope) // resolvedType = type)
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
        val body = Parser.parseDefinitionsAndStatements(tokenizer, ParsingContext(functionScope, context.depth + 1))

        return LambdaReference(type, functionScope.locals.size, body)
    }

    fun createNumberLiteral(s: String) =
        if (s.contains('.') || s.contains('e') || s.contains('E'))
            FloatNode.Const(s.toDouble()) else IntNode.Const(s.toLong())

    fun parsePrimary(tokenizer: TantillaTokenizer, context: ParsingContext) =
        when (tokenizer.current.type) {
            TokenType.NUMBER -> createNumberLiteral(tokenizer.next().text);
            TokenType.STRING -> StrNode.Const(tokenizer.next().text.unquote().unescape())
            TokenType.MULTILINE_STRING ->StrNode.Const(tokenizer.next().text.unquoteMultiline(), true)
            TokenType.IDENTIFIER ->  when (tokenizer.current.text) {
                "True" -> {
                    tokenizer.consume("True")
                    BoolNode.True
                }
                "False" -> {
                    tokenizer.consume("False")
                    BoolNode.True
                }
                "lambda" -> parseLambda(tokenizer, context)
                else -> parseFreeIdentifier(tokenizer, context)
            }
            else -> {
                val mutable = tokenizer.tryConsume("mut")
                when (tokenizer.current.text) {
                    "(" -> {
                        if (mutable) {
                            throw tokenizer.exception("'[' expected after mut.")
                        }
                        tokenizer.consume("(")
                        val result = parseExpression(tokenizer, context)
                        tokenizer.consume(")")
                        result
                    }
                    "[" -> {
                        tokenizer.consume("[")
                        ListLiteral(parseList(tokenizer, context, "]"), false)
                    }
                    else -> throw tokenizer.exception("Number, identifier or opening bracket expected here.")
                }
            }
        }

    fun parseList(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
        endMarker: String): List<Node> {
        val result = mutableListOf<Node>()
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
        base: Node,
    ): Node {
        val traitName = tokenizer.consume(TokenType.IDENTIFIER)
        val className = base.returnType.typeName
        val impl = context.scope.resolveStaticOrError("$traitName for $className", true).getValue(null) as ImplDefinition
        impl.resolveAll(CompilationResults())
        return As(base, impl, implicit = false)
    }

    fun parseProperty(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
        base: Node,
    ): Node {
        val name = tokenizer.consume(TokenType.IDENTIFIER)
        val baseType = base.returnType
        val definition = baseType.resolve(name)
            ?: throw tokenizer.exception("Property '$name' not found.")
        return property(tokenizer, context, base, definition)
    }

    fun property(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
        base: Node,
        definition: Definition
    ): Node {
        var self: Node? = null
        val name = definition.name
        val value = when (definition.kind) {
            Definition.Kind.PROPERTY -> PropertyReference(base, definition)
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
        value: Node,
        self: Node?,
        openingParenConsumed: Boolean,
        asMethod: Boolean
    ): Node {
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
        val parameterExpressions = MutableList<Node?>(expectedParameters.size) { null }
        val parameterSerialization = mutableListOf<Apply.ParameterSerialization>()
        var index = 0
        if (self != null) {
            parameterExpressions[index++] = self
            if (!asMethod) {
                parameterSerialization.add(Apply.ParameterSerialization("", self))
            }
        }

        val indexMap = mutableMapOf<String, Int>()
        for (i in expectedParameters.indices) {
            indexMap[expectedParameters[i].name] = i
        }

        val varargs = mutableListOf<Node>()
        var varargIndex = -1
        var nameRequired = false
        if (hasArgs && !tokenizer.tryConsume(")")) {
            do {
                var name = ""
                if (tokenizer.current.type == TokenType.IDENTIFIER && tokenizer.lookAhead(1).text == "=") {
                    name = tokenizer.consume(TokenType.IDENTIFIER)
                    tokenizer.consume("=")
                    nameRequired = true
                    index = indexMap[name] ?: throw tokenizer.exception("Parameter name '$name' not found.")
                } else if (nameRequired) {
                    throw tokenizer.exception("Named parameter required here.")
                } else if (index >= expectedParameters.size) {
                    throw tokenizer.exception("Expected parameters $expectedParameters exceeded; index: $index")
                }
                val expectedParameter = expectedParameters[index]
                val expression = parseExpression(tokenizer, context, expectedParameter.type)
                parameterSerialization.add(Apply.ParameterSerialization(name, expression))
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
            parameterExpressions[varargIndex] = ListLiteral(varargs, false)
        }

        for (i in expectedParameters.indices) {
            val expectedParameter = expectedParameters[i]
            if (parameterExpressions[i] == null) {
                if (expectedParameter.defaultValueExpression == null) {
                    if (expectedParameter.isVararg) {
                        parameterExpressions[i] = ListLiteral(varargs, false)
                    } else {
                        throw tokenizer.exception("Parameter '${expectedParameter.name}' is missing.")
                    }
                } else {
                    parameterExpressions[i] = expectedParameter.defaultValueExpression
                }
            }
        }

        return Apply(
            value,
            List(parameterExpressions.size) { parameterExpressions[it]!!},
            parameterSerialization.toList(),
            !hasArgs,
            asMethod
        )
    }


    val expressionParser =
        GreenspunExpressionParser<TantillaTokenizer, ParsingContext, Node>(
            GreenspunExpressionParser.suffix(Precedence.DOT, ".") {
                tokenizer, context, _, base -> parseProperty(tokenizer, context, base) },
            GreenspunExpressionParser.suffix(Precedence.BRACKET, "[") {
                tokenizer, context, _, base -> parseElementAt(tokenizer, context, base)
            },
            GreenspunExpressionParser.suffix(Precedence.BRACKET, "(") {
                tokenizer, context, _, base -> parseMaybeApply(
                tokenizer, context, base, self =null, openingParenConsumed = true, asMethod = false) },
            GreenspunExpressionParser.infix(Precedence.POW, "**") { _, _, _, l, r -> FloatNode.Pow(l, r)},
            GreenspunExpressionParser.infix(Precedence.MULDIV, "*") { _, _, _, l, r -> if (bothInt(l, r)) IntNode.Mul(l, r) else FloatNode.Mul(l, r)},
            GreenspunExpressionParser.infix(Precedence.MULDIV, "//") { _, _, _, l, r -> IntNode.Div(l, r)},
            GreenspunExpressionParser.infix(Precedence.MULDIV, "/") { _, _, _, l, r -> FloatNode.Div(l, r)},
            GreenspunExpressionParser.infix(Precedence.MULDIV, "%") { _, _, _, l, r -> if (bothInt(l, r)) IntNode.Mod(l, r) else FloatNode.Mod(l, r)},
            GreenspunExpressionParser.prefix(Precedence.UNARY, "~") { _, _, _, expr -> IntNode.Not(expr)},
            GreenspunExpressionParser.prefix(Precedence.UNARY, "-") { _, _, _, expr -> if (expr.returnType == org.kobjects.tantilla2.core.type.IntType) IntNode.Neg(expr) else FloatNode.Neg(expr)},
            GreenspunExpressionParser.infix(Precedence.PLUSMINUS, "+") { _, _, _, l, r -> if (l.returnType == StrType) StrNode.Add(l, r) else if (bothInt(l, r)) IntNode.Add(l, r) else FloatNode.Add(l, r)},
            GreenspunExpressionParser.infix(Precedence.PLUSMINUS, "-") { _, _, _, l, r ->  if (bothInt(l, r)) IntNode.Sub(l, r) else FloatNode.Sub(l, r)},
            GreenspunExpressionParser.infix(Precedence.BITWISE_SHIFT, "<<") { _, _, _, l, r -> IntNode.Shl(l, r)},
            GreenspunExpressionParser.infix(Precedence.BITWISE_SHIFT, ">>") { _, _, _, l, r -> IntNode.Shr(l, r)},
            GreenspunExpressionParser.infix(Precedence.BITWISE_AND, "&") { _, _, _, l, r -> IntNode.And(l, r)},
            GreenspunExpressionParser.infix(Precedence.BITWISE_XOR, "^") { _, _, _, l, r -> IntNode.Xor(l, r)},
            GreenspunExpressionParser.infix(Precedence.BITWISE_OR, "|") { _, _, _, l, r -> IntNode.Or(l, r)},
            GreenspunExpressionParser.infix(Precedence.RELATIONAL, "in") { _, _, _, l, r, -> CollectionNode.In(l, r)},
            GreenspunExpressionParser.suffix(Precedence.RELATIONAL, "as") {
                    tokenizer, context, _, base -> parseAs(tokenizer, context, base) },
            GreenspunExpressionParser.infix(Precedence.RELATIONAL, "==") { _, _, _, l, r ->  if (bothInt(l, r)) IntNode.Eq(l, r) else if (bothNumber(l, r)) FloatNode.Eq(l, r) else StrNode.Eq(l, r) },
            GreenspunExpressionParser.infix(Precedence.RELATIONAL, "!=") { _, _, _, l, r ->  if (bothInt(l, r)) IntNode.Ne(l, r) else  if (bothNumber(l, r)) FloatNode.Ne(l, r) else StrNode.Ne(l, r)},
            GreenspunExpressionParser.infix(Precedence.RELATIONAL, "<") { _, _, _, l, r ->  if (bothInt(l, r)) IntNode.Lt(l, r) else FloatNode.Lt(l, r)},
            GreenspunExpressionParser.infix(Precedence.RELATIONAL, ">") { _, _, _, l, r ->  if (bothInt(l, r)) IntNode.Gt(l, r) else FloatNode.Gt(l, r)},
            GreenspunExpressionParser.infix(Precedence.RELATIONAL, "<=") { _, _, _, l, r ->  if (bothInt(l, r)) IntNode.Le(l, r) else FloatNode.Le(l, r)},
            GreenspunExpressionParser.infix(Precedence.RELATIONAL, ">=") { _, _, _, l, r ->  if (bothInt(l, r)) IntNode.Ge(l, r) else FloatNode.Ge(l, r)},
            GreenspunExpressionParser.prefix(Precedence.LOGICAL_NOT, "not") { _, _, _, expr -> BoolNode.Not(expr)},
            GreenspunExpressionParser.infix(Precedence.LOGICAL_AND, "and") { _, _, _, l, r -> BoolNode.And(l, r)},
            GreenspunExpressionParser.infix(Precedence.LOGICAL_OR, "or") { _, _, _, l, r -> BoolNode.Or(l, r)},
        ) {
                t, c -> parsePrimary(t, c)
        }


}