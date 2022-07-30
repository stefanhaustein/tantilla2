package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.Type

class PropertyReference(
    val base: Evaluable<RuntimeContext>,
    val name: String,
    val definition: Definition
) : Assignable {
    override val returnType: Type
        get() = definition.type

    override fun children(): List<Evaluable<RuntimeContext>> = emptyList()

    override fun eval(ctx: RuntimeContext): Any? {
        val self = base.eval(ctx)
        return definition.getValue(self)
    }

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>): Evaluable<RuntimeContext> =
        PropertyReference(newChildren[0], name, definition)

    override fun assign(context: RuntimeContext, value: Any?) {
        val self = base.eval(context)
        definition.setValue(self, value)
    }

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.appendCode(base)
        writer.append('.').append(name)
    }

    override fun toString() = CodeWriter().appendCode(this).toString()

}