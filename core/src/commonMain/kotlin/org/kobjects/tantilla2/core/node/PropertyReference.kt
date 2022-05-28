package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.Type

class PropertyReference(
    val base: Evaluable<RuntimeContext>,
    val name: String,
    override val returnType: Type,
    val index: Int,
    val mutable: Boolean
) : Assignable {
    override fun children(): List<Evaluable<RuntimeContext>> = emptyList()

    override fun eval(ctx: RuntimeContext): Any? {
        val self = base.eval(ctx) as RuntimeContext
        return self.variables[index]
    }

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>): Evaluable<RuntimeContext> =
        PropertyReference(newChildren[0], name, returnType, index, mutable)

    override fun assign(context: RuntimeContext, value: Any?) {
        val self = base.eval(context) as RuntimeContext
        self.variables[index] = value
    }

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.appendCode(base)
        writer.append('.').append(name)
    }

}