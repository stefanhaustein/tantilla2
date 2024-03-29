package org.kobjects.tantilla2.core.node.statement

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.NoneType
import org.kobjects.tantilla2.core.node.LeafNode

class Comment(val text: String?) : LeafNode() {
    override val returnType: Type
        get() = NoneType

    override fun eval(context: LocalRuntimeContext): Any = NoneType.None

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        if (text != null) {
            writer.appendComment(text)
        }
    }
}