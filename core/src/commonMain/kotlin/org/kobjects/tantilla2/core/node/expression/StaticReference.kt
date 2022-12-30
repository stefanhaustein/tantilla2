package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.node.AssignableNode
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.Type


data class StaticReference(val definition: Definition, val qualified: Boolean, val raw: Boolean) : AssignableNode() {
    override fun children() = emptyList<Node>()

    override fun eval(ctx: LocalRuntimeContext): Any = definition.getValue(ctx.globalRuntimeContext.staticVariableValues)

    override fun reconstruct(newChildren: List<Node>) = this

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        val parent = definition.parentScope
        if (qualified) {
            (parent as Type).serializeType(writer)
//            writer.append(parent!!.qualifiedName())
            if (!raw) {
                writer.append('.')
            }
        }
        if (raw) {
            writer.append("::")
        }
        writer.append(definition.name)
    }

    override fun assign(context: LocalRuntimeContext, value: Any) = definition.setValue(context.globalRuntimeContext.staticVariableValues, value)

    override val returnType: Type
        get() = definition.type

}