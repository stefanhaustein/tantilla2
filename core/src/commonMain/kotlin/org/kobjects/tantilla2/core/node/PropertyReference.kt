package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Type

class PropertyReference(
    val base: Evaluable,
    val definition: Definition
) : Assignable() {
    override val returnType: Type
        get() = definition.type

    override fun children(): List<Evaluable> = emptyList()

    override fun eval(ctx: LocalRuntimeContext): Any? {
        val self = base.eval(ctx)
        return definition.getValue(self)
    }

    override fun reconstruct(newChildren: List<Evaluable>): Evaluable =
        PropertyReference(newChildren[0], definition)

    override fun assign(context: LocalRuntimeContext, value: Any?) {
        val self = base.eval(context)
        definition.setValue(self, value)
    }

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendCode(base)
        writer.append('.').append(definition.name)
    }


}