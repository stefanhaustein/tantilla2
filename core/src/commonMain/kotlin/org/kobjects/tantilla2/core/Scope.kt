package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.classifier.UserClassDefinition
import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.function.FunctionScope
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunction
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.parser.ParsingContext
import org.kobjects.tantilla2.core.parser.TantillaTokenizer
import org.kobjects.tantilla2.core.runtime.Void

abstract class Scope(
    val parentContext: Scope?
): SerializableCode, Iterable<Definition> {
    var docString: String = ""
    private val definitions = mutableMapOf<String, Definition>()
    var locals = mutableListOf<String>()
    abstract val title: String

/*
    override val type: Type
        get() = if (this is ClassDefinition) ClassMetaType(this) else MetaType(this)

*/

    fun add(definition: Definition) {
        definitions[definition.name] = definition
        if (definition.kind == Definition.Kind.LOCAL_VARIABLE && definition.index == -1) {
            definition.index = locals.size
            locals.add(definition.name)
        }
    }

    fun findNode(node: Evaluable<RuntimeContext>): Definition? {
        for (definition in definitions.values) {
            val result = definition.findNode(node)
            if (result != null) {
                return result
            }
        }
        return null
    }


    operator fun get(name: String): Definition? = definitions[name]

    override fun iterator(): Iterator<Definition> = definitions.values.iterator()

    fun declareLocalVariable(name: String, type: Type, mutable: Boolean): Int {
        val definition = Definition(
            this,
            Definition.Kind.LOCAL_VARIABLE,
            name,
            mutable = mutable,
            resolvedType = type
        )
        add(definition)
        return definition.index
    }


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
            val name = oldDefinition?.name ?: "[error]"
            replacement = Definition(
                this,
                Definition.Kind.UNPARSEABLE,
                name,
                definitionText = newContent
            )
        }
        definitions[replacement.name] = replacement
        return replacement
    }


    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        val sb = StringBuilder()

        when (this) {
            is UserClassDefinition -> writer.keyword("struct").append(' ').declaration(name)
            is TraitDefinition -> writer.keyword("trait").append(' ').declaration(name)
            is ImplDefinition -> writer.keyword("impl").append(' ').declaration(name)
            is FunctionScope -> {
                sb.append("lambda ")
            }
        }
        writer.indent()
        for (definition in definitions.values) {
            writer.newline()
            writer.newline()
            writer.appendCode(definition)
        }
        writer.outdent()
    }

    fun resolveDynamic(name: String, fallBackToStatic: Boolean): Definition? {
        val result = definitions[name]
        if (result != null) {
            if (fallBackToStatic || result.isDyanmic()) {
                return result
            }
        }
        if (this is FunctionScope && parentContext is FunctionScope) {
            return parentContext.resolveDynamic(name, fallBackToStatic)
        }
        
        if (fallBackToStatic) {
            return resolveStatic(name, true)
        }
        return null
    }

    fun resolveStatic(name: String, fallBackToParent: Boolean = false): Definition? {
        val result = definitions[name]
        if (result != null) {
            if (result.isDyanmic()) {
                throw RuntimeException("Static property expected; found: $result")
            }
            return result
        }
        if (fallBackToParent && parentContext != null) {
            return parentContext.resolveStatic(name, true)
        }
        return null
    }

    open fun hasError(): Boolean {
        var result = false
        for (definition in definitions.values) {
            if (definition.hasError(true)) {
                result = true
            }
        }
        return result
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
        val function = NativeFunction(type, operation)
        definitions[name] = Definition(
            this,
            Definition.Kind.FUNCTION,
            name,
            resolvedType = type,
            resolvedValue = function,
            docString = docString
        )
    }

    fun remove(name: String) {
        val removed = definitions.remove(name)
        if (removed != null && removed.index != -1) {
            locals.remove(name)
            for (definition in iterator()) {
                if (definition.index > removed.index) {
                    definition.index--
                }
            }
        }
    }

    override fun toString() = title

}