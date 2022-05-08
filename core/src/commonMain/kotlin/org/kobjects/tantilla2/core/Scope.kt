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
) {
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


    override fun toString() = serialize()

    fun serialize(indent: String = ""): String {
        val sb = StringBuilder()
        val innerIndent = "  $indent"

        when (this) {
            is ClassDefinition -> sb.append("${indent}class $name:\n")
            is TraitDefinition -> sb.append("${indent}trait $name:\n")
            is ImplDefinition -> sb.append("${indent}impl $name:\n")
            is FunctionScope -> {
                sb.append("${indent}lambda ")
            }
        }

        for (definition in definitions.values) {
            sb.append(definition.serialize(innerIndent)).append('\n')
        }

        return sb.toString()
    }

    fun resolve(name: String): Definition {
        return definitions[name] ?: (parentContext?.resolve(name)
            ?: throw RuntimeException("Undefined: '$name'"))
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