package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.*


data class StaticReference(val definition: Definition, val qualified: Boolean) : TantillaNode, Assignable {
    override fun children() = emptyList<Evaluable>()

    override fun eval(ctx: LocalRuntimeContext): Any? = definition.getValue(ctx.globalRuntimeContext.staticVariableValues)

    override fun reconstruct(newChildren: List<Evaluable>) = this

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        val parent = definition.parentScope
        if (qualified) {
            writer.append(parent!!.name)
            writer.append('.')
        }
        writer.append(definition.name)
    }

    override fun assign(context: LocalRuntimeContext, value: Any?) = definition.setValue(context.globalRuntimeContext.staticVariableValues, value)

    override val returnType: Type
        get() = definition.type

    override fun toString(): String = definition.name
}