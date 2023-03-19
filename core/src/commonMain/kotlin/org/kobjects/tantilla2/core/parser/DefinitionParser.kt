import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.parserlib.tokenizer.Token
import org.kobjects.tantilla2.core.classifier.LazyImplDefinition
import org.kobjects.tantilla2.core.classifier.StructDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.definition.*
import org.kobjects.tantilla2.core.function.FunctionDefinition
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.parser.Parser.indent
import org.kobjects.tantilla2.core.parser.ParsingContext
import org.kobjects.tantilla2.core.parser.TantillaScanner
import org.kobjects.tantilla2.core.parser.TokenType
import org.kobjects.tantilla2.core.scope.*

object DefinitionParser {

    fun parseFailsafe(parentScope: Scope, code: String): Definition {
        val tokenizer = TantillaScanner(code)
        val startPos = tokenizer.current
        var result: Definition
        try {
            result = parseDefinition(tokenizer, ParsingContext(parentScope, -1))

            while (tokenizer.current.type == TokenType.LINE_BREAK) {
                tokenizer.consume()
            }

            if (tokenizer.current.type != TokenType.EOF) {
                result = UnparseableDefinition(parentScope, /* result.name,*/ definitionText = CodeFragment(startPos, code))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            //            val name = oldDefinition?.name ?: "[error]"
            result = UnparseableDefinition(
                parentScope,
                definitionText = CodeFragment(startPos, code)
            )
        }
        return result
    }

    fun parseDefinitionFailsafe(tokenizer: TantillaScanner, context: ParsingContext, errors: MutableList<ParsingException>?): Definition {
        if (errors == null) {
            return parseDefinition(tokenizer, context)
        }
        val startPos = tokenizer.current
        try {
            return parseDefinition(tokenizer, context)
        } catch (e: Exception) {
            e.printStackTrace()
            val parsingException = tokenizer.ensureParsingException(e)
            errors.add(parsingException)
            val text = Parser.consumeBody(tokenizer, startPos, context.depth)
            return UnparseableDefinition(context.scope, definitionText = text)
        }
    }

    fun parseDefinition(tokenizer: TantillaScanner, context: ParsingContext): Definition {
        val startPos = tokenizer.current
        val explicitlyStatic = tokenizer.tryConsume("static")
        var mutable = tokenizer.tryConsume("mut")
        val scope = context.scope

        if ((tokenizer.current.type == TokenType.IDENTIFIER
                    && (tokenizer.lookAhead(1).text == "=" || tokenizer.lookAhead(1).text == ":"))) {
            val local = !explicitlyStatic && (scope is StructDefinition || scope is FunctionDefinition)
            return parseFieldDeclaration(tokenizer, context, startPos, local, mutable)
        }

        if (mutable) {
            throw tokenizer.exception("'mut' seems to be misplaced here.")
        }

        return when (val kind = tokenizer.current.text) {
            "enum" -> parseEnum(tokenizer, context)
            "def" -> {
                val isMethod = !explicitlyStatic
                        && (scope is StructDefinition || scope is TraitDefinition || scope is LazyImplDefinition)
                tokenizer.consume("def")
                mutable = tokenizer.tryConsume("mut")
                if (tokenizer.lookAhead(1).text == ":" || tokenizer.lookAhead(1).text == "=") {
                    val local = !explicitlyStatic && (scope is StructDefinition || scope is FunctionDefinition)
                    parseFieldDeclaration(tokenizer, context, startPos, local, mutable)
                } else {
                    if (mutable) {
                        throw tokenizer.exception("'mut' seems to be misplaced here.")
                    }
                    val name = tokenizer.consume(TokenType.IDENTIFIER) { "Name expected." }.text
                    println("consuming def $name")
                    val text = Parser.consumeBody(tokenizer, startPos, context.depth)
                    FunctionDefinition(
                        context.scope,
                        if (isMethod) Definition.Kind.METHOD else Definition.Kind.FUNCTION,
                        name,
                        definitionText = text
                    )
                }
            }
            "import" -> {
                tokenizer.consume(kind)
                val path = mutableListOf<String>()
                do {
                    path.add(tokenizer.consume(TokenType.IDENTIFIER).text)
                } while (tokenizer.tryConsume("."))
                ImportDefinition(context.scope, path)
            }
            "unit",
            "struct",
            "trait"-> {
                tokenizer.consume(kind)
                val name = tokenizer.consume(TokenType.IDENTIFIER) { "'${it.text}' is not a valid name for $kind" }.text
                tokenizer.consume(":") { "Colon expected after the $kind name."}
                val docString = Parser.readDocString(tokenizer)
                val definition = when (kind) {
                    "struct" -> StructDefinition(context.scope, name, docString = docString)
                    "unit" -> UnitScope(context.scope, name, docString = docString)
                    else -> TraitDefinition(context.scope, name, docString = docString)
                }
                Parser.parseDefinitions(tokenizer, ParsingContext(definition, context.depth + 1))
                definition
            }
            "impl" -> {
                tokenizer.consume("impl")
                val traitName = tokenizer.consume(TokenType.IDENTIFIER) { "Trait name expected." }.text
                tokenizer.consume("for") { "'for' expected after trait name for impl." }
                val scopeName = tokenizer.consume(TokenType.IDENTIFIER) { "Struct name expected after 'for'." }.text
                tokenizer.consume(":") { "Colon expected after 'for ${scopeName}' for impl." }
                val docString = Parser.readDocString(tokenizer)
                val impl = LazyImplDefinition(
                    context.scope,
                    traitName = traitName,
                    scopeName = scopeName,
                    docString = docString
                )
                Parser.parseDefinitions(tokenizer, ParsingContext(impl, context.depth + 1))
                impl
            }
            else -> throw tokenizer.exception("Declaration expected.")
        }
    }

    fun parseEnum(tokenizer: TantillaScanner, parsingContext: ParsingContext): EnumDefinition {
        tokenizer.consume("enum")
        val name = tokenizer.consume(TokenType.IDENTIFIER).text
        val enumDefinition = EnumDefinition(parsingContext.scope, name)

        tokenizer.consume(":")
        while (tokenizer.current.type == TokenType.LINE_BREAK && tokenizer.current.text.indent() > parsingContext.depth) {
            tokenizer.consume(TokenType.LINE_BREAK)
            val literalName = tokenizer.consume(TokenType.IDENTIFIER).text
            enumDefinition.add(EnumLiteral(enumDefinition, literalName))
        }
        return enumDefinition
    }


    fun parseFieldDeclaration(
        tokenizer: TantillaScanner,
        context: ParsingContext,
        startPos: Token<TokenType>,
        local: Boolean,
        mutable: Boolean,
    ) : FieldDefinition {
        val name = tokenizer.consume(TokenType.IDENTIFIER).text
        val kind = if (local) Definition.Kind.PROPERTY
        else Definition.Kind.STATIC
        val text = Parser.consumeLine(tokenizer, startPos)

        return FieldDefinition(context.scope, kind, name, mutable = mutable, definitionText = text)
    }

}