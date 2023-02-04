package org.kobjects.tantilla2.core.node.statement

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.control.LoopControlSignal
import org.kobjects.tantilla2.core.node.LeafNode
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.NoneType

class LoopSignalNode(
    val kind: LoopControlSignal.Kind,
) : LeafNode() {
    override val returnType: Type
        get() = NoneType

    override fun children(): List<Node> = emptyList()

    override fun eval(context: LocalRuntimeContext): LoopControlSignal {
        throw LoopControlSignal(kind)
    }

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
       writer.appendKeyword(kind.name.lowercase())
    }

}