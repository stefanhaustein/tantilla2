package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.Unparseable
import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.classifier.NativePropertyDefinition
import org.kobjects.tantilla2.core.function.*
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.parser.ParsingContext
import org.kobjects.tantilla2.core.parser.TantillaTokenizer

abstract class Scope(
): Definition, Iterable<Definition> {
    var error: Exception? = null

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


    fun update(newContent: String, oldDefinition: Definition? = null): Definition {
        if (oldDefinition != null) {
            definitions.remove(oldDefinition.name)
        }
        val tokenizer = TantillaTokenizer(newContent)
        tokenizer.next()
        var replacement: Definition
        try {
            replacement = Parser.parseDefinition(tokenizer, ParsingContext(this, 0))
        } catch (e: Exception) {
            e.printStackTrace()
            //            val name = oldDefinition?.name ?: "[error]"
            replacement = Unparseable(
                this,
                definitionText = newContent
            )
        }
        definitions[replacement.name] = replacement

        return replacement
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

    fun resolveStatic(name: String, fallBackToParent: Boolean = false): Definition? {
        val result = definitions[name]
        if (result != null) {
            if (result.isDynamic()) {
                throw RuntimeException("Static property expected; found: $result")
            }
            return result
        }
        val parent = parentScope
        if (fallBackToParent && parent != null) {
            return parent.resolveStatic(name, true)
        }
        return null
    }


    fun defineNativeFunction(
        name: String,
        docString: String,
        returnType: Type,
        vararg parameter: Parameter,
        operation: (RuntimeContext) -> Any?) {
        val type = object : FunctionType {
            override val returnType = returnType
            override val parameters = parameter.toList()
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


    override val errors: List<Exception>
        get() {
            if (error == null) {
                try {
                    getValue(null)
                } catch (e: Exception) {
                    println("Error in $parentScope.$name")
                    e.printStackTrace()
                }
            }
            val error = error
            return if (error == null) emptyList() else listOf(error)
    }

    override fun resolveAll(compilationResults: CompilationResults): Boolean {
        var childError = errors.isNotEmpty()
        for (definition in this) {
            if (!definition.resolveAll(compilationResults)) {
                childError = true
            } else if (definition is ImplDefinition) {
                compilationResults.classToTrait.getOrPut(definition.struct) { mutableMapOf() }[definition.trait] = this
                compilationResults.traitToClass.getOrPut(definition.trait) { mutableMapOf() }[definition.struct] = this
            }
        }
        if (childError) {
            compilationResults.definitionsWithErrors.add(this)
        }
        return !childError
    }

    override fun getValue(self: Any?) = this

    override fun toString() = name

    override fun serializeTitle(writer: CodeWriter, abbreviated: Boolean) {
        writer.appendKeyword(kind.name.lowercase()).append(' ').appendDeclaration(name)
    }

    fun serializeBody(writer: CodeWriter) {
        for (definition in this) {
            writer.appendCode(definition)
            writer.newline()
            writer.newline()
        }
    }

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.appendKeyword(kind.name.lowercase()).append(' ').appendDeclaration(name).append(":")
        writer.indent()
        writer.newline()
        if (docString.isNotEmpty()) {
            writer.appendTripleQuoted(docString)
            writer.newline()
        }
        serializeBody(writer)
        writer.outdent()
    }

    override fun serializeSummary(writer: CodeWriter) {
        serializeTitle(writer)
        writer.append(":")
        if(docString.isNotEmpty()) {
            writer.newline()
            writer.appendWrapped(CodeWriter.Kind.STRING, docString)
        }
        writer.indent()
        val scope = getValue(null) as Scope
        for (definition in scope.iterator()) {
            writer.newline()
            definition.serializeTitle(writer, abbreviated = true)
        }
        writer.outdent()
    }

    override fun isDynamic() = false

    override fun isScope() = errors.isEmpty()

    override fun findNode(node: Evaluable<RuntimeContext>): Definition? {
        for (definition in this) {
            val result = definition.findNode(node)
            if (result != null) {
                return result
            }
        }
        return null
    }

    override fun initialize() {
        for (definition in this) {
            definition.initialize()
        }
    }

    override fun reset() {
        error = null
        for (definition in this) {
            definition.reset()
        }
    }


}