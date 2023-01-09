package org.kobjects.tantilla2.core.parser

import org.kobjects.tantilla2.core.node.expression.StrNode
import org.kobjects.tantilla2.core.*
import org.kobjects.parserlib.expressionparser.ExpressionParser as GreenspunExpressionParser
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.classifier.InstantiableMetaType
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.LambdaScope
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.node.expression.*
import org.kobjects.tantilla2.core.node.expression.Apply
import org.kobjects.tantilla2.core.parser.TypeParser.parseType
import org.kobjects.tantilla2.core.type.*

object ExpressionParser {

    fun parseExpression(tokenizer: TantillaTokenizer, context: ParsingContext, expectedType: Type? = null): Node {
        if (expectedType is FunctionType) {
            return parseFunctionExpression(tokenizer, context, expectedType)
        }
        val result = expressionParser.parse(tokenizer, context)
        return matchType(result, expectedType)
    }


    fun matchType(expr: Node, expectedType: Type?): Node {
        val actualType = expr.returnType
        if (expectedType == null || expectedType.isAssignableFrom(actualType)) {
            return expr
        }

        if (expectedType is TraitDefinition) {
            val impl = expectedType.requireImplementationFor(actualType)
            return As(expr, impl, implicit = true)
        }

        throw IllegalArgumentException("Can't convert $expr with type '${expr.returnType}' to '$expectedType'")
    }

    fun parseElementAt(tokenizer: TantillaTokenizer, context: ParsingContext, base: Node): Node {
        // tokenizer.disable(TokenType.LINE_BREAK)

        if (base.returnType is InstantiableMetaType
            && (base.returnType as InstantiableMetaType).wrapped.genericParameterTypes.isNotEmpty()
        ) {
            val typeParameters = mutableListOf<Type>()
            do {
                typeParameters.add(TypeParser.parseType(tokenizer, context))
            } while(tokenizer.tryConsume(","))
            tokenizer.consume("]")
       //     tokenizer.enable(TokenType.LINE_BREAK)
            return StaticReference(((base.returnType) as InstantiableMetaType).wrapped.withGenericsResolved(typeParameters) as Definition, true)
            // GenericTypeNode(base, typeParameters)
        }

        val result = ElementAt(base, parseExpression(tokenizer, context))
        //tokenizer.enable(TokenType.LINE_BREAK)
        tokenizer.consume("]")
        return result
    }

    fun reference(scope: Scope, definition: Definition, qualified: Boolean) =
        if (definition.kind == Definition.Kind.PROPERTY) {
            val depth = definition.depth(scope)
            LocalVariableReference(
                definition.name, definition.type, depth, definition.index, definition.mutable)
        } else StaticReference(definition, qualified)

    fun isCallable(definition: Definition) =
        definition.getValue(null) is Callable // Resolve imports


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
                asMethod = false)
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
        if (staticDefinition != null && (tokenizer.current.text != "(" || isCallable(staticDefinition)) ) {
            return parseMaybeApply(
                tokenizer,
                context,
                reference(scope, staticDefinition, false),
                self = null,
                openingParenConsumed = false,
                asMethod = false)
        }

        // Method in function form
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
                    asMethod = false)
            }
        }

        throw tokenizer.exception("Symbol not found: '$name'.")
    }

    fun parseFunctionExpression(tokenizer: TantillaTokenizer,
                                context: ParsingContext,
                                type: FunctionType): Node {
        if (tokenizer.current.text == "lambda") {
            return parseLambda(tokenizer, context, type)
        }
        val functionScope = LambdaScope(context.scope) // resolvedType = type)
        for (i in type.parameters.indices) {
            val parameter = type.parameters[i]
            functionScope.declareLocalVariable("\$$i", parameter.type, false)
        }
        val body = parseExpression(tokenizer, ParsingContext(functionScope, context.depth), type.returnType)
        return LambdaReference(type, functionScope.locals.size, body, implicit = true)
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

        println("*** Lambda type parsed: $type")

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



        val body = Parser.parseDefinitionsAndStatements(tokenizer, context.depth + 1, functionScope, definitionScope = functionScope)

        println("*** Lambda body parsed: $body")


        return LambdaReference(type, functionScope.locals.size, body, implicit = false)
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
                    BoolNode.False
                }
                "lambda" -> parseLambda(tokenizer, context)
                else -> parseFreeIdentifier(tokenizer, context)
            }
            else -> {
                when (tokenizer.current.text) {
                    "(" -> {
                        tokenizer.consume("(")
                                //  tokenizer.disable(TokenType.LINE_BREAK)

                        val expression = parseExpression(tokenizer, context)

                        tokenizer.consume(")")
                                //tokenizer.enable(TokenType.LINE_BREAK)
                        Parentesized(expression)
                    }
                    "[" -> ListLiteral(parseList(tokenizer, context, "[", "]"))
                    else -> throw tokenizer.exception("Number, identifier or opening bracket expected here, got: '${tokenizer.current.text}'.")
                }
            }
        }

    fun parseList(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
        startMarker: String,
        endMarker: String
    ): List<Node> {
        tokenizer.consume(startMarker)
        //tokenizer.disable(TokenType.LINE_BREAK)

        val result = mutableListOf<Node>()
        if (tokenizer.current.text != endMarker) {
            do {
                result.add(parseExpression(tokenizer, context))
            } while (tokenizer.tryConsume(","))
        }
        tokenizer.consume(endMarker, "$endMarker or , expected")
       // tokenizer.enable(TokenType.LINE_BREAK)

        return result.toList()
    }

    fun parseAs(
        tokenizer: TantillaTokenizer,
        context: ParsingContext,
        base: Node,
    ): Node {
        val trait = parseType(tokenizer, context) as TraitDefinition
        val impl = trait.requireImplementationFor(base.returnType)
        // impl.resolveAll()
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
        definition: Definition,
    ): Node {
        var self: Node? = null
        val value = when (definition.kind) {
            Definition.Kind.PROPERTY -> PropertyReference(base, definition)
            Definition.Kind.METHOD -> {
                 self = base
                 StaticReference(definition, false)
                }
            else -> StaticReference(definition, true)
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
        asMethod: Boolean,
    ): Node {
        if (!openingParenConsumed && tokenizer.tryConsume("@")) {
            require(!asMethod) { "Can't combine @ with methods." }
           return RawNode(value)
        }

        val type = value.returnType

        if (type !is FunctionType) {
            // Not a function, just skip () and error otherwise

            if (openingParenConsumed || tokenizer.tryConsume("(")) {
                tokenizer.consume(")", "Empty parameter list expected.")
            }
            return value
        }

        // Don't imply constructor calls.
        val parentesizedArgsList = openingParenConsumed || tokenizer.tryConsume("(")
        if (!parentesizedArgsList && type is InstantiableMetaType) {
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

        val parseParameterList = if (parentesizedArgsList) !tokenizer.tryConsume(")")
        else ((type.returnType == VoidType || type.hasRequiredParameters())
                && tokenizer.current.type != TokenType.EOF
                && tokenizer.current.type != TokenType.LINE_BREAK
                && tokenizer.current.text != ":"
                && !asMethod
                && self == null)

        if (parseParameterList)  {
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
                val expression =
                    parseExpression(tokenizer, context, expectedParameter.type)

                parameterSerialization.add(Apply.ParameterSerialization(name, expression))
                if (expectedParameter.isVararg) {
                    varargs.add(expression)
                    varargIndex = index
                } else {
                    parameterExpressions[index++] = expression
                }
            } while (tokenizer.tryConsume(","))

            if (parentesizedArgsList) {
                tokenizer.consume(")")
            }
        }

        if (varargIndex != -1) {
            parameterExpressions[varargIndex] = ListLiteral(varargs)
        }

        for (i in expectedParameters.indices) {
            val expectedParameter = expectedParameters[i]
            if (parameterExpressions[i] == null) {
                if (expectedParameter.defaultValueExpression == null) {
                    if (expectedParameter.isVararg) {
                        parameterExpressions[i] = ListLiteral(varargs)
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
            parentesizedArgsList,
            asMethod
        )
    }


    val expressionParser =
        GreenspunExpressionParser<TantillaTokenizer, ParsingContext, Node>(
            GreenspunExpressionParser.suffix(Precedence.DOT, ".") {
                tokenizer, context, _, base -> parseProperty(tokenizer, context, base) },
            GreenspunExpressionParser.suffix(Precedence.DOT, "::") {
                    tokenizer, context, _, base -> parseProperty(tokenizer, context, base) },
            GreenspunExpressionParser.suffix(Precedence.BRACKET, "[") {
                tokenizer, context, _, base -> parseElementAt(tokenizer, context, base) },
            GreenspunExpressionParser.suffix(Precedence.BRACKET, "(") {
                tokenizer, context, _, base ->
                parseMaybeApply(
                    tokenizer,
                    context,
                    base,
                    self =null,
                    openingParenConsumed = true,
                    asMethod = false) },
            GreenspunExpressionParser.infix(Precedence.POW, "**") {
                    _, _, _, l, r -> FloatNode.Pow(l, r) },
            GreenspunExpressionParser.infix(Precedence.MULDIV, "*") {
                    _, _, _, l, r -> NodeFactory.mul(l, r) },
            GreenspunExpressionParser.infix(Precedence.MULDIV, "//") {
                    _, _, _, l, r -> IntNode.Div(l, r) },
            GreenspunExpressionParser.infix(Precedence.MULDIV, "/") {
                    _, _, _, l, r -> FloatNode.Div(l, r) },
            GreenspunExpressionParser.infix(Precedence.MULDIV, "%") {
                    _, _, _, l, r -> NodeFactory.mod(l, r) },
            GreenspunExpressionParser.prefix(Precedence.UNARY, "~") {
                    _, _, _, expr -> IntNode.Not(expr) },
            GreenspunExpressionParser.prefix(Precedence.UNARY, "-") {
                    _, _, _, expr -> NodeFactory.neg(expr)},
            GreenspunExpressionParser.infix(Precedence.PLUSMINUS, "+") {
                    _, _, _, l, r -> NodeFactory.add(l, r)},
            GreenspunExpressionParser.infix(Precedence.PLUSMINUS, "-") {
                    _, _, _, l, r ->  NodeFactory.sub(l, r)},
            GreenspunExpressionParser.infix(Precedence.BITWISE_SHIFT, "<<") { _, _, _, l, r -> IntNode.Shl(l, r)},
            GreenspunExpressionParser.infix(Precedence.BITWISE_SHIFT, ">>") { _, _, _, l, r -> IntNode.Shr(l, r)},
            GreenspunExpressionParser.infix(Precedence.BITWISE_AND, "&") { _, _, _, l, r -> IntNode.And(l, r)},
            GreenspunExpressionParser.infix(Precedence.BITWISE_XOR, "^") { _, _, _, l, r -> IntNode.Xor(l, r)},
            GreenspunExpressionParser.infix(Precedence.BITWISE_OR, "|") { _, _, _, l, r -> IntNode.Or(l, r)},
            GreenspunExpressionParser.infix(Precedence.RELATIONAL, "in") { _, _, _, l, r, -> CollectionNode.In(l, r)},
            GreenspunExpressionParser.suffix(Precedence.RELATIONAL, "as") {
                    tokenizer, context, _, base -> parseAs(tokenizer, context, base) },
            GreenspunExpressionParser.infix(Precedence.RELATIONAL, "==") { _, _, _, l, r -> NodeFactory.eq(l, r)},
            GreenspunExpressionParser.infix(Precedence.RELATIONAL, "!=") { _, _, _, l, r ->  NodeFactory.ne(l, r)},
            GreenspunExpressionParser.infix(Precedence.RELATIONAL, "<") { _, _, _, l, r ->  NodeFactory.lt(l, r)},
            GreenspunExpressionParser.infix(Precedence.RELATIONAL, ">") { _, _, _, l, r ->  NodeFactory.gt(l, r)},
            GreenspunExpressionParser.infix(Precedence.RELATIONAL, "<=") { _, _, _, l, r ->  NodeFactory.le(l, r)},
            GreenspunExpressionParser.infix(Precedence.RELATIONAL, ">=") { _, _, _, l, r ->  NodeFactory.ge(l, r)},
            GreenspunExpressionParser.prefix(Precedence.LOGICAL_NOT, "not") { _, _, _, expr -> BoolNode.Not(expr)},
            GreenspunExpressionParser.infix(Precedence.LOGICAL_AND, "and") { _, _, _, l, r -> BoolNode.And(l, r)},
            GreenspunExpressionParser.infix(Precedence.LOGICAL_OR, "or") { _, _, _, l, r -> BoolNode.Or(l, r)},
        ) {
                t, c -> parsePrimary(t, c)
        }


}