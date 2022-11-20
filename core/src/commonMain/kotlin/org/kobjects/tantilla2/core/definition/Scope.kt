package org.kobjects.tantilla2.core.definition

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.CompilationResults
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.type.RootScope
import org.kobjects.tantilla2.core.classifier.FieldDefinition
import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.classifier.NativePropertyDefinition
import org.kobjects.tantilla2.core.classifier.Updatable
import org.kobjects.tantilla2.core.function.*
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.parser.ParsingContext
import org.kobjects.tantilla2.core.parser.TantillaTokenizer
import org.kobjects.tantilla2.core.parser.TokenType
import org.kobjects.tantilla2.core.type.Type

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

    fun sorted() = definitions.values.toList().sorted()

    fun checkErrors(newProperty: String): List<Exception> {
        val tokenizer = TantillaTokenizer(newProperty)
        tokenizer.next()
        var replacement = try {
            Parser.parseDefinition(tokenizer, ParsingContext(this, 0))
        } catch (e: Exception) {
            e.printStackTrace()
            return listOf(e)
        }
        if (replacement.errors.isNotEmpty()) {
            return replacement.errors
        }
        while (tokenizer.current.type == TokenType.LINE_BREAK) {
            tokenizer.next()
        }
        if (tokenizer.current.type != TokenType.EOF) {
            return listOf(tokenizer.exception("End of input expected"))
        }
        return emptyList()
    }

    fun update(newContent: String, oldDefinition: Definition? = null): Definition {
        var replacement = Parser.parseFailsafe(this, newContent)
        if (oldDefinition != null) {
            try {
                if (oldDefinition is Updatable && replacement is Updatable
                    && oldDefinition.type == replacement.type
                    && oldDefinition.kind == replacement.kind
                ) {
                    oldDefinition.definitionText = replacement.definitionText
                    return oldDefinition
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            definitions.remove(oldDefinition.name)
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

    fun resolveStaticOrError(name: String, fallBackToParent: Boolean = false): Definition {
        return resolveStatic(name, fallBackToParent) ?: throw RuntimeException("$name not found in $this")
    }

    fun defineNativeFunction(
        name: String,
        docString: String,
        returnType: Type,
        vararg parameter: Parameter,
        operation: (LocalRuntimeContext) -> Any?) {
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
                compilationResults.classToTrait.getOrPut(definition.scope) { mutableMapOf() }[definition.trait] = this
                compilationResults.traitToClass.getOrPut(definition.trait) { mutableMapOf() }[definition.scope] = this
            }
        }
        if (childError) {
            compilationResults.definitionsWithErrors.add(this)
        }
        return !childError
    }

    open fun registerStatic(fieldDefinition: FieldDefinition): Int =
        parentScope!!.registerStatic(fieldDefinition)

    override fun getValue(self: Any?) = this

    override fun toString() = name

    override fun serializeTitle(writer: CodeWriter, abbreviated: Boolean) {
        writer.appendKeyword(kind.name.lowercase()).append(' ').appendDeclaration(name)
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
        writer.appendKeyword(kind.name.lowercase()).append(' ').appendDeclaration(name).append(":")
        writer.indent()
        writer.newline()

        serializeBody(writer)
        writer.outdent()
    }

    override fun serializeSummary(writer: CodeWriter) {
        serializeTitle(writer)
        writer.append(":")
        if(docString.isNotEmpty()) {
            writer.newline()
            writer.appendWrapped(CodeWriter.Kind.STRING, docString.split("\n").first())
        }
        writer.indent()
        val scope = getValue(null) as Scope
        for (definition in scope.sorted()) {
            writer.newline()
            definition.serializeTitle(writer, abbreviated = true)
        }
        writer.outdent()
    }

    override fun isDynamic() = false

    override fun isScope() = errors.isEmpty()

    override fun findNode(node: Node): Definition? {
        for (definition in this) {
            val result = definition.findNode(node)
            if (result != null) {
                return result
            }
        }
        return null
    }

    override fun reset() {
        error = null
        for (definition in this) {
            definition.reset()
        }
    }

    fun typeName(type: Definition): String {
        // Check for imports
        var scope: Scope? = this
        while (scope != null) {
            for (definition in scope.definitions.values) {
                if (definition is ImportDefinition && definition.getValue(null) == type) {
                    return definition.name
                }
            }
            scope = scope.parentScope
        }
        // Construct the fully qualified name.
        val sb = StringBuilder()
        scope = type.parentScope
        while (scope != null && scope !is RootScope && scope !is UserRootScope) {
            sb.insert(0, '.')
            sb.insert(0, scope.name)
            scope = scope.parentScope
        }
        sb.append(type.name)
        return sb.toString()
    }


}