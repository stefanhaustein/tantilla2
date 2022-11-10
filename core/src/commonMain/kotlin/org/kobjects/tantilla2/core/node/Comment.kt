package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.builtin.VoidType

class Comment(val text: String) : TantillaNode {
    override val returnType: Type
        get() = VoidType

    override fun children(): List<Evaluable> = listOf()

    override fun eval(context: LocalRuntimeContext): Any? = null

    override fun reconstruct(newChildren: List<Evaluable>): Evaluable = this

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.appendComment(text)
    }
}