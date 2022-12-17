package org.kobjects.tantilla2.core.node.statement

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.VoidType
import org.kobjects.tantilla2.core.node.LeafNode

class Comment(val text: String) : LeafNode() {
    override val returnType: Type
        get() = VoidType


    override fun eval(context: LocalRuntimeContext): Any = VoidType.None

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendComment(text)
    }
}