package org.kobjects.tantilla2.core.classifier

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.node.Assignable

class PropertyReference(
    val base: Evaluable<LocalRuntimeContext>,
    val definition: Definition
) : Assignable {
    override val returnType: Type
        get() = definition.type

    override fun children(): List<Evaluable<LocalRuntimeContext>> = emptyList()

    override fun eval(ctx: LocalRuntimeContext): Any? {
        val self = base.eval(ctx)
        return definition.getValue(self)
    }

    override fun reconstruct(newChildren: List<Evaluable<LocalRuntimeContext>>): Evaluable<LocalRuntimeContext> =
        PropertyReference(newChildren[0], definition)

    override fun assign(context: LocalRuntimeContext, value: Any?) {
        val self = base.eval(context)
        definition.setValue(self, value)
    }

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.appendCode(base)
        writer.append('.').append(definition.name)
    }

    override fun toString() = CodeWriter().appendCode(this).toString()

}