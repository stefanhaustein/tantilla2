package org.kobjects.tantilla2.core.definition

import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.classifier.*
import org.kobjects.tantilla2.core.function.*
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.parser.ParsingContext
import org.kobjects.tantilla2.core.parser.TantillaScanner
import org.kobjects.tantilla2.core.type.Type

abstract class Scope(
): Definition, Iterable<Definition> {
    private val definitions = mutableMapOf<String, Definition>()
    val locals = mutableListOf<String>()

    open val supportsMethods: Boolean
        get() = false

    open val supportsLocalVariables: Boolean
        get() = false

    fun add(definition: Definition) {
        definitions[definition.name] = definition
        if (definition.kind == Definition.Kind.PROPERTY && definition !is NativePropertyDefinition && definition.index == -1) {
            definition.index = locals.size
            locals.add(definition.name)
        }
    }

    fun declareLocalVariable(name: String, type: Type, mutable: Boolean): Int {
        val definition = LocalVariableDefinition(
            this,
            name,
            mutable = mutable,
            type = type
        )
        add(definition)
        return definition.index
    }

    operator fun get(name: String): Definition? = definitions[name]

    override fun iterator(): Iterator<Definition> = definitions.values.iterator()

    fun sorted() = definitions.values.toList().sorted()

    fun checkErrors(newContent: String, oldDefinition: Definition?): List<ParsingException> {
        val tokenizer = TantillaScanner(newContent)
        val errors = mutableListOf<ParsingException>()
        Parser.parseDefinitions(tokenizer, ParsingContext(this, 0), errorCollector = errors, definitionCallback = { startToken, it ->
            try {
                if (definitions.containsKey(it.name) && it.name != oldDefinition?.name) {
                    errors.add(ParsingException(startToken, "A property named '${it.name}' already exists in this scope."))
                }
                println("****** Resolve: $it")
                it.resolve(applyOffset = true, errorCollector = errors)
            } catch (e: Exception) {
                println("****** Resolving failed: $e")
                errors.add(tokenizer.ensureParsingException(e))
            }
        })
        return errors.toList()
    }

    fun update(newContent: String, oldDefinition: Definition? = null) {
        val newDefinitions = mutableMapOf<String, Definition>()
        val tokenizer = TantillaScanner(newContent)

        Parser.parseDefinitions(tokenizer, ParsingContext(this, 0), errorCollector = mutableListOf(), definitionCallback =  { startToken, it ->
            if (definitions.containsKey(it.name) && it.name != oldDefinition?.name) {
                val unparseable = UnparseableDefinition(this, definitionText = CodeFragment(startToken, CodeWriter(highlighting = CodeWriter.defaultHighlighting).appendCode(it).toString()))
                newDefinitions[unparseable.name] = unparseable
            } else {
                newDefinitions[it.name] = it
            }
        })

        if (oldDefinition != null) {
            val replacement = newDefinitions[oldDefinition.name] ?: newDefinitions.values.firstOrNull()
            var remove = true
            // There may be issues with resolving the type or resolve.
            try {
                if (oldDefinition is DefinitionUpdatable
                    && replacement is DefinitionUpdatable
                    && oldDefinition.name == replacement.name
                    && oldDefinition.type == replacement.type
                    && replacement.kind == oldDefinition.kind
                ) {
                    oldDefinition.definitionText = replacement.definitionText
                    newDefinitions.remove(replacement.name)
                    remove = false
                    oldDefinition.invalidate()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (remove) {
                definitions.remove(oldDefinition.name)
            }
        }

        if (newDefinitions.isNotEmpty()) {
            for (definition in newDefinitions.values) {
                add(definition)
            }
            userRootScope().invalidateAll()
        }
    }


    fun resolveDynamic(name: String, fallBackToStatic: Boolean): Definition? {
        val result = definitions[name]
        if (result != null && (result.isDynamic() || fallBackToStatic)) {
            return result
        }
        val parent = parentScope
        // TODO: Document this check (looks like nested function support)
        if ((this is FunctionDefinition || this is LambdaScope)
            && (parent is FunctionDefinition || parent is LambdaScope)) {
            return parent.resolveDynamic(name, fallBackToStatic)
        }
        
        if (fallBackToStatic) {
            return resolveStatic(name, true)
        }
        return null
    }

    fun remove(name: String) {
        val removed = definitions.remove(name)
        if (removed != null && removed.index != -1) {
            locals.remove(name)
            for (definition in this) {
                if (definition.index > removed.index) {
                    definition.index--
                }
            }
        }
    }

    fun resolveStatic(propertyName: String, fallBackToParent: Boolean = false): Definition? {
        val result = definitions[propertyName]
        if (result != null) {
            if (result.isDynamic()) {
                throw RuntimeException("Reference to dynamic property '${propertyName}' on static type '${name}'. Did you mean to use an object instead of a type?")
            }
            return result
        }
        val parent = parentScope
        if (fallBackToParent && parent != null) {
            return parent.resolveStatic(propertyName, true)
        }
        return null
    }

    fun resolveStaticOrError(name: String, fallBackToParent: Boolean = false): Definition {
        return resolveStatic(name, fallBackToParent) ?: throw RuntimeException("$name not found in $this")
    }

    fun defineNativeFunction(
        name: String,
        docString: String,
        returnType: Type,
        vararg parameter: Parameter,
        operation: (LocalRuntimeContext) -> Any) {
        val type = object : FunctionType {
            override val returnType = returnType
            override val parameters = parameter.toList()
            override fun serializeType(writer: CodeWriter) {
                FunctionType.serializeType(this, writer)
            }
        }
        definitions[name] = NativeFunctionDefinition(
            this,
            if (parameter.isNotEmpty() && parameter[0].name == "self") Definition.Kind.METHOD else Definition.Kind.FUNCTION,
            name,
            docString,
            type,
            operation
        )
    }

    fun recurse(action: (Definition) -> Unit) {
        action(this)
        for (child in this) {
            if (child is Scope) {
                child.recurse(action)
            } else {
                action(child)
            }
        }
    }

    /*
    override fun resolveAll(): Boolean {
        var childError = false
        for (definition in this) {
            if (!definition.resolveAll()) {
                childError = true
            }
        }
        val localError = !super.resolveAll()
        if (childError && !localError) {
            userRootScope().definitionsWithErrors.put(this, listOf())
        }
        return !childError && !localError
    }*/

    open fun registerStatic(fieldDefinition: FieldDefinition): Int =
        parentScope!!.registerStatic(fieldDefinition)

    override fun getValue(self: Any?) = this

    override fun toString() = if (this is Type) CodeWriter().appendType(this).toString() else name

    private fun serializeTitle(writer: CodeWriter) {
        writer.appendKeyword(kind.name.lowercase()).append(' ').appendDeclaration(name)
        if (this is Type) {
            serializeGenerics(writer)
        }
    }

    fun serializeBody(writer: CodeWriter) {
        if (docString.isNotEmpty()) {
            writer.appendTripleQuoted(docString)
            writer.newline()
        }
        for (definition in this) {
            writer.appendCode(definition)
            writer.newline()
            writer.newline()
        }
    }

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        serializeTitle(writer)
        writer.append(":")
        writer.indent()
        writer.newline()

        writer.enterScope(this)
        serializeBody(writer)
        writer.leaveScope()

        writer.outdent()
    }

    override fun isSummaryExpandable() = definitions.isNotEmpty() || docString.isNotEmpty()


    override fun serializeSummary(writer: CodeWriter, kind: Definition.SummaryKind) {
        serializeTitle(writer)
        if (kind == Definition.SummaryKind.EXPANDED) {
            writer.forTitle = true
            writer.append(":")
            if (docString.isNotEmpty()) {
                writer.newline()
                writer.appendWrapped(CodeWriter.Kind.STRING, docString.split("\n").first())
            }
            writer.indent()
            val scope = getValue(null) as Scope
            for (definition in scope.sorted()) {
                writer.newline()
                definition.serializeSummary(writer, Definition.SummaryKind.NESTED)
            }
            writer.outdent()
        }
    }

    override fun isDynamic() = false

    override fun findNode(node: Node): Definition? {
        for (definition in this) {
            val result = definition.findNode(node)
            if (result != null) {
                return result
            }
        }
        return null
    }

}