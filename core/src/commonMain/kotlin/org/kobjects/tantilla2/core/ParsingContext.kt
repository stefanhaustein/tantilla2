package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type
import org.kobjects.greenspun.core.Void
import org.kobjects.tantilla2.core.Serializer.serialize

class ParsingContext(
    override val name: String,
    val kind: Kind,
    val parentContext: ParsingContext?
): Type, Typed, Lambda {
    val definitions = mutableMapOf<String, Definition>()
    var locals = mutableListOf<Definition>()
    var traitIndex = 0

    override val type: Type
        get() = if (kind == Kind.CLASS) ClassMetaType(this) else MetaType(this)

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

        when (kind) {
            Kind.CLASS -> sb.append("${indent}class $name:\n")
            Kind.FUNCTION -> {
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

    enum class Kind {
        ROOT, CLASS, FUNCTION, TRAIT, IMPL
    }


}