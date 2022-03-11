package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type
import org.kobjects.greenspun.core.Void

class AssignLocal(
    val index: Int,
    val value: Evaluable<RuntimeContext>,
) : Evaluable<RuntimeContext> {
    override val type: Type
        get() = Void

    override fun children(): List<Evaluable<RuntimeContext>> {
        return listOf(value)
    }

    override fun eval(context: RuntimeContext): Any? {
        context.variables[index] = value.eval(context)
        return Unit
    }

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>): Evaluable<RuntimeContext> {
        return AssignLocal(index, newChildren[0])
    }
}