package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.*


data class StaticReference(val definition: Definition) : TantillaNode {
    override fun children() = emptyList<Evaluable<RuntimeContext>>()

    override fun eval(ctx: RuntimeContext): Any? = definition.value()

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) = this

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.append(definition.name)
    }

    override val returnType: Type
        get() = definition.valueType()

    override fun toString(): String = definition.name
}