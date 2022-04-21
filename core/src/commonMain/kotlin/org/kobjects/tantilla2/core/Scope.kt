package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.classifier.ClassDefinition
import org.kobjects.tantilla2.classifier.ClassMetaType
import org.kobjects.tantilla2.classifier.ImplDefinition
import org.kobjects.tantilla2.classifier.TraitDefinition
import org.kobjects.tantilla2.function.Callable
import org.kobjects.tantilla2.function.FunctionScope

abstract class Scope(
    override val name: String,
    val parentContext: Scope?
): Type, Typed, Callable {
    val definitions = mutableMapOf<String, Definition>()
    var locals = mutableListOf<Definition>()


    override val type: Type
        get() = if (this is ClassDefinition) ClassMetaType(this) else MetaType(this)

    override fun eval(context: RuntimeContext) = context

    fun declareLocalVariable(name: String, type: Type, mutable: Boolean): Int {
        val index = locals.size
        val definition = Definition(
            this,
            name,
            Definition.Kind.LOCAL_VARIABLE,
            type = type,
            index = index,
            mutable = mutable)
        definitions[name] = definition
        locals.add((definition))
        return index
    }

    fun defineValue(name: String, value: Any) {
        definitions[name] = Definition(this, name, Definition.Kind.CONST, value = value)
    }

    fun defineDelayed(kind: Definition.Kind, name: String, definition: String) {
        definitions[name] = Definition(this, name, kind, definitionText = definition)
    }

    override fun toString() = serialize()

    fun serialize(indent: String = ""): String {
        val sb = StringBuilder()
        val innerIndent = "  $indent"

        when (this) {
            is ClassDefinition -> sb.append("${indent}class $name:\n")
            is TraitDefinition -> sb.append("${indent}trait $name:\n")
            is ImplDefinition -> sb.append("${indent}impl $name:\n")
            is FunctionScope -> {
                sb.append("${indent}def $name")
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