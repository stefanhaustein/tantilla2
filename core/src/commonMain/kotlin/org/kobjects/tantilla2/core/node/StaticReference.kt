package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.runtime.RootScope


data class StaticReference(val definition: Definition, val qualified: Boolean) : TantillaNode {
    override fun children() = emptyList<Evaluable<RuntimeContext>>()

    override fun eval(ctx: RuntimeContext): Any? = definition.value

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) = this

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        val parent = definition.parentScope
        if (qualified) {
            writer.append(parent!!.name)
            writer.append('.')
        }
        writer.append(definition.name)
    }

    override val returnType: Type
        get() = definition.type

    override fun toString(): String = definition.name
}