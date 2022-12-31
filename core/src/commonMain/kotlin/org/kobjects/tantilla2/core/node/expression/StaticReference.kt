package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.Type


data class StaticReference(val definition: Definition, val qualified: Boolean, val raw: Boolean) : Node() {
    override fun children() = emptyList<Node>()

    override fun eval(ctx: LocalRuntimeContext): Any = definition.getValue(ctx.globalRuntimeContext.staticVariableValues)

    override fun reconstruct(newChildren: List<Node>) = this

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        if (qualified) {
            writer.append(definition.qualifiedName(raw = raw, relativeTo = writer.scope))
            if (definition is Type) {
                definition.serializeGenerics(writer)
            }
        } else {
            writer.append(definition.name)
        }
    }

    override fun assign(context: LocalRuntimeContext, value: Any) = definition.setValue(context.globalRuntimeContext.staticVariableValues, value)

    override val returnType: Type
        get() = definition.type

    override fun requireAssignability() {
        if (!definition.mutable) {
            throw IllegalStateException("Definition $definition is not mutable.")
        }
    }

    init {
        if (!qualified) {
            require(!raw) { "Can't combine !qualified with raw." }
        }
    }
}