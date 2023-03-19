package org.kobjects.tantilla2.core.parser

import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.node.expression.StrNode
import org.kobjects.tantilla2.core.*
import org.kobjects.parserlib.expressionparser.ExpressionParser as GreenspunExpressionParser
import org.kobjects.tantilla2.core.classifier.InstantiableMetaType
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.LocalVariableDefinition
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.node.expression.*
import org.kobjects.tantilla2.core.parser.TypeParser.parseType
import org.kobjects.tantilla2.core.type.*

object TantillaExpressionParser {

    fun parseExpression(tokenizer: TantillaScanner, context: ParsingContext, expectedType: Type? = null, genericTypeMap: GenericTypeMap? = null): Node {
        if (expectedType is FunctionType) {
            return LambdaParser.parseFunctionExpression(tokenizer, context, expectedType, genericTypeMap)
        }
        val result = expressionParser.parse(tokenizer, context)
        return matchType(context, result, expectedType, genericTypeMap)
    }


    fun matchType(context: ParsingContext, expr: Node, expectedType: Type?, genericTypeMap: GenericTypeMap? = null): Node {

        if (expectedType == null) {
            return expr
        }
        val actualType = expr.returnType

        val resolvedType = if (genericTypeMap != null) {
            println("ExpectedType: $expectedType actualType: $actualType")
            expectedType.resolveGenerics(actualType, genericTypeMap, true, context.scope.userRootScope())
        } else {
            expectedType
        }

        if (resolvedType.isAssignableFrom(actualType) || resolvedType == NoneType) {
            return expr
        }

        if (resolvedType is TraitDefinition) {
            val impl = resolvedType.requireImplementationFor(context.scope.userRootScope(), actualType)
            println("Constructing 'as' with type ${impl.trait}")
            return As(expr, impl, impl.trait, implicit = true)
        }

        throw IllegalArgumentException("Can't convert $expr with type '${expr.returnType}' to '$resolvedType'")
    }

    fun parseElementAt(tokenizer: TantillaScanner, context: ParsingContext, base: Node): Node {
        if (base.returnType is InstantiableMetaType
            && (base.returnType as InstantiableMetaType).wrapped.genericParameterTypes.isNotEmpty()
        ) {
            val type = (base.returnType as InstantiableMetaType).wrapped
            val genericTypeList = TypeParser.parseGenericTypeList(tokenizer, context)
            return StaticReference(type.withGenericsResolved(genericTypeList) as Definition, true)
        }
        val result = ElementAt(base, parseExpression(tokenizer, context))
        tokenizer.consume("]")
        return result
    }

    /** A reference that doesn't require a "special" context -- either static or a local variable */
    fun reference(scope: Scope, definition: Definition, qualified: Boolean) =
        if (definition.kind == Definition.Kind.PROPERTY) {
            val depth = definition.depth(scope)
            LocalVariableReference(definition as LocalVariableDefinition, depth)
        } else StaticReference(definition, qualified)

    fun isCallable(definition: Definition) =
        definition.getValue(null) is Callable // Resolve imports


    fun parseFreeIdentifier(tokenizer: TantillaScanner, context: ParsingContext): Node {
        val nameToken = tokenizer.consume(TokenType.IDENTIFIER)
        val name = nameToken.text
        val scope = context.scope

        val dynamicDefinition = scope.resolveDynamic(name, fallBackToStatic = false)
        if (dynamicDefinition != null && dynamicDefinition.isDynamic()) {
            return ApplyParser.parseMaybeApply(
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
            return ApplyParser.parseMaybeApply(
                tokenizer,
                context,
                reference(scope, staticDefinition, false),
                self = null,
                openingParenConsumed = false,
                asMethod = false)
        }

        // Method in function form
        if (tokenizer.tryConsume("(") && tokenizer.current.text != ")") {
            val firstParameter = parseExpression(tokenizer, context)
            if (firstParameter.returnType !is Scope) {
                throw tokenizer.exception("Return type ${firstParameter.returnType} of $firstParameter is not a Scope!")
            }
            val baseType = firstParameter.returnType as Scope
            val definition = baseType[name]
            if (!tokenizer.tryConsume(",") && tokenizer.current.text != ")") {
                throw tokenizer.exception("Comma or closing paren expected after first parameter.")
            }
            if (definition != null) {
                return ApplyParser.parseMaybeApply(
                    tokenizer,
                    context,
                    reference(context.scope, definition, false),
                    firstParameter,
                    openingParenConsumed = true,
                    asMethod = false)
            }
        }

        throw ParsingException(nameToken, "Symbol not found: '$name'.")
    }



    fun createNumberLiteral(s: String) =
        if (s.contains('.') || s.contains('e') || s.contains('E'))
            FloatNode.Const(s.toDouble()) else IntNode.Const(s.toLong())

    fun parsePrimary(tokenizer: TantillaScanner, context: ParsingContext): Node =
        when (tokenizer.current.type) {
            TokenType.NUMBER -> createNumberLiteral(tokenizer.consume().text);
            TokenType.STRING -> StrNode.Const(tokenizer.consume().text.unquote().unescape())
            TokenType.MULTILINE_STRING ->StrNode.Const(tokenizer.consume().text.unquote(), true)
            TokenType.IDENTIFIER ->  when (tokenizer.current.text) {
                "True", "true" -> {
                    tokenizer.consume()
                    BoolNode.True
                }
                "False", "false" -> {
                    tokenizer.consume()
                    BoolNode.False
                }
                "lambda" -> LambdaParser.parseLambda(tokenizer, context)
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
        tokenizer: TantillaScanner,
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
        tokenizer.consume(endMarker) { "'$endMarker' or ',' expected here." }
       // tokenizer.enable(TokenType.LINE_BREAK)

        return result.toList()
    }

    fun parseAs(
        tokenizer: TantillaScanner,
        context: ParsingContext,
        base: Node,
    ): Node {
        val trait = parseType(tokenizer, context) as TraitDefinition
        val impl = trait.requireImplementationFor(context.scope.userRootScope(), base.returnType)
        // impl.resolveAll()
        return As(base, impl, trait)
    }

    fun parseProperty(
        tokenizer: TantillaScanner,
        context: ParsingContext,
        base: Node,
    ): Node {
        val name = tokenizer.consume(TokenType.IDENTIFIER).text
        val baseType = base.returnType
        val definition = baseType.resolve(name)
            ?: throw tokenizer.exception("Property '$name' not found.")
        return property(tokenizer, context, base, definition)
    }

    fun property(
        tokenizer: TantillaScanner,
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
        return ApplyParser.parseMaybeApply(
            tokenizer,
            context,
            value,
            self,
            openingParenConsumed = false,
            asMethod = self != null)
    }




    val expressionParser =
        GreenspunExpressionParser<TantillaScanner, ParsingContext, Node>(
            GreenspunExpressionParser.suffix(Precedence.DOT, ".") {
                tokenizer, context, _, base -> parseProperty(tokenizer, context, base) },
            GreenspunExpressionParser.suffix(Precedence.DOT, "::") {
                    tokenizer, context, _, base -> parseProperty(tokenizer, context, base) },
            GreenspunExpressionParser.suffix(Precedence.BRACKET, "[") {
                tokenizer, context, _, base -> parseElementAt(tokenizer, context, base) },
            GreenspunExpressionParser.suffix(Precedence.BRACKET, "(") {
                tokenizer, context, _, base ->
                ApplyParser.parseMaybeApply(
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