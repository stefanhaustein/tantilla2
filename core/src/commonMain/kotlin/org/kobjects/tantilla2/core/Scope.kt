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
import org.kobjects.tantilla2.core.parser.TantillaTokenizer

abstract class Scope(
    val parentContext: Scope?
): SerializableCode {
    var docString: String = ""
    val definitions = mutableMapOf<String, Definition>()
    var locals = mutableListOf<String>()
    abstract val title: String

/*
    override val type: Type
        get() = if (this is ClassDefinition) ClassMetaType(this) else MetaType(this)

*/


    fun declareLocalVariable(name: String, type: Type, mutable: Boolean): Int {
        val definition = Definition(
            this,
            name,
            Definition.Kind.LOCAL_VARIABLE,
            resolvedType = type,
            mutable = mutable)
        definitions[name] = definition
        locals.add(name)
        return locals.size - 1
    }


    fun update(newContent: String, oldDefinition: Definition? = null) {
        if (oldDefinition != null) {
            definitions.remove(oldDefinition.name)
        }
        val tokenizer = TantillaTokenizer(newContent)
        tokenizer.next()
        var replacement: Definition
        try {
            replacement = Parser.parseDefinition(tokenizer, this, 0)
        } catch (e: Exception) {
            e.printStackTrace()
            val name = oldDefinition?.name ?: "[error]"
            replacement = Definition(
                this,
                name,
                Definition.Kind.UNPARSEABLE,
                definitionText = newContent
            )
        }
        definitions[replacement.name] = replacement
    }


    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        val sb = StringBuilder()

        when (this) {
            is UserClassDefinition -> writer.keyword("class").append(' ').declaration(name)
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

    fun resolveDynamic(name: String, fallBackToStatic: Boolean = false): Definition {
        val result = definitions[name]
        if (result != null) {
            if (fallBackToStatic || result.isDyanmic()) {
                return result
            }
            throw IllegalStateException("Dynamic property expected; found: $result")
        }
        if (fallBackToStatic) {
            return resolveStatic(name, true)
        }
        throw IllegalStateException("Not found: '$name'")
    }

    fun resolveStatic(name: String, fallBackToParent: Boolean = false): Definition {
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
        throw RuntimeException("Undefined: '$name'")
    }

    open fun resolveAll(): Boolean {
        var allOk = true
        for (definition in definitions.values) {
            try {
                val value = definition.value()
                if (value is Scope && !value.resolveAll()) {
                    allOk = false
                }
            } catch (e: Exception) {
                allOk = false
            }
        }
        return allOk
    }


    fun defineNative(
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
            name,
            Definition.Kind.FUNCTION,
            resolvedType = type,
            resolvedValue = function,
            docString = docString
        )
    }


}