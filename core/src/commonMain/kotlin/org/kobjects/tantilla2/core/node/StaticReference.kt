package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.*


data class StaticReference(val definition: Definition, val qualified: Boolean) : TantillaNode, Assignable {
    override fun children() = emptyList<Evaluable<LocalRuntimeContext>>()

    override fun eval(ctx: LocalRuntimeContext): Any? = definition.getValue(null)

    override fun reconstruct(newChildren: List<Evaluable<LocalRuntimeContext>>) = this

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        val parent = definition.parentScope
        if (qualified) {
            writer.append(parent!!.name)
            writer.append('.')
        }
        writer.append(definition.name)
    }

    override fun assign(context: LocalRuntimeContext, value: Any?) = definition.setValue(null, value)

    override val returnType: Type
        get() = definition.type

    override fun toString(): String = definition.name
}