package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Precedence
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.node.Node

class PropertyReference(
    val base: Node,
    val definition: Definition,
) : Node() {
    override val returnType: Type
        get() = definition.type

    override fun children(): List<Node> = listOf(base)

    override fun eval(ctx: LocalRuntimeContext): Any {
        val self = base.eval(ctx)
        return definition.getValue(self)
    }

    override fun reconstruct(newChildren: List<Node>): Node =
        PropertyReference(newChildren[0], definition)

    override fun assign(context: LocalRuntimeContext, value: Any) {
        val self = base.eval(context)
        definition.setValue(self, value)
    }

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendInfix(parentPrecedence, base, ".", Precedence.DOT, RawIdentifier(definition.name))
    }

    override fun requireAssignability(): Node {
        if (!definition.mutable) {
            throw IllegalStateException("Property $definition is not mutable.")
        }
        return this
    }
}