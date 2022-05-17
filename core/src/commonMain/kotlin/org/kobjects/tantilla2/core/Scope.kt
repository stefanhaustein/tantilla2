package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.classifier.ClassDefinition
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

    fun createLocalVariable(name: String, type: Type, mutable: Boolean, initializer: Evaluable<RuntimeContext>?, builtin: Boolean = false) =
        Definition(
            this,
            name,
            Definition.Kind.LOCAL_VARIABLE,
            explicitType = type,
            builtin = builtin,
            mutable = mutable,
            initializer = initializer)


    fun declareLocalVariable(name: String, type: Type, mutable: Boolean, builtin: Boolean = false): Int {
        val definition = createLocalVariable(name, type, mutable, null, builtin)
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
            replacement = Definition(this, name, Definition.Kind.UNPARSEABLE, definitionText = newContent)
        }
        definitions[replacement.name] = replacement
    }

    fun createUnparsed(kind: Definition.Kind, name: String, definition: String) =
        Definition(this, name, kind, definitionText = definition)


    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        val sb = StringBuilder()

        when (this) {
            is ClassDefinition -> writer.keyword("class").append(' ').declaration(name)
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

    fun resolve(name: String, includeLocals: Boolean = true): Definition {
        val result = definitions[name]
        return if (result == null || (!includeLocals && result.kind == Definition.Kind.LOCAL_VARIABLE))
            parentContext?.resolve(name, false) ?: throw RuntimeException("Undefined: '$name'")
            else result
    }

    open fun resolveAll() {
        for (definition in definitions.values) {
            definition.value()
        }
    }


    fun defineNative(
        name: String,
        docString: String,
        returnType: Type,
        vararg parameter: Parameter,
        operation: (RuntimeContext) -> Any?) {
        val type = FunctionType(returnType, parameter.toList())
        val function = NativeFunction(type, operation)
        definitions[name] = Definition(
            this,
            name,
            Definition.Kind.FUNCTION,
            explicitType = type,
            explicitValue = function,
            builtin = true,
            docString = docString
        )
    }

}