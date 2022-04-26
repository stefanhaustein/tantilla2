package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.classifier.ClassDefinition
import org.kobjects.tantilla2.core.classifier.ClassMetaType
import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionScope

abstract class Scope(
    val parentContext: Scope?
) {
    val definitions = mutableMapOf<String, Definition>()
    var locals = mutableListOf<String>()

/*
    override val type: Type
        get() = if (this is ClassDefinition) ClassMetaType(this) else MetaType(this)

*/

    fun createLocalVariable(name: String, type: Type, mutable: Boolean, initializer: Evaluable<RuntimeContext>?) =
        Definition(
            this,
            name,
            Definition.Kind.LOCAL_VARIABLE,
            type = type,
            builtin = false,
            mutable = mutable,
            initializer = initializer)

    fun declareLocalVariable(name: String, type: Type, mutable: Boolean): Int {
        val definition = createLocalVariable(name, type, mutable, null)
        definitions[name] = definition
        locals.add(name)
        return locals.size - 1
    }


    fun createValue(name: String, value: Any, builtin: Boolean = false) =
        Definition(this, name, Definition.Kind.CONST, builtin = builtin, value = value)

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



}