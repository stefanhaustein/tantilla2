package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.node.AssignableNode
import org.kobjects.tantilla2.core.node.Node

class PropertyReference(
    val base: Node,
    val definition: Definition,
    val raw: Boolean
) : AssignableNode() {
    override val returnType: Type
        get() = definition.type

    override fun children(): List<Node> = emptyList()

    override fun eval(ctx: LocalRuntimeContext): Any {
        val self = base.eval(ctx)
        return definition.getValue(self)
    }

    override fun reconstruct(newChildren: List<Node>): Node =
        PropertyReference(newChildren[0], definition, raw)

    override fun assign(context: LocalRuntimeContext, value: Any) {
        val self = base.eval(context)
        definition.setValue(self, value)
    }

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendCode(base)
        writer.append(if (raw) "::" else ".").append(definition.name)
    }


}