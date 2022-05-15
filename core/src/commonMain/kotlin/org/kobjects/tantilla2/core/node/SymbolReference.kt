package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.SerializableCode


data class SymbolReference(
    val name: String,
    override val type: Type,
    val value: Any?) : Evaluable<RuntimeContext>, SerializableCode {
    override fun children() = emptyList<Evaluable<RuntimeContext>>()

    override fun eval(ctx: RuntimeContext): Any? = value

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) = this

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.append(name)
    }
}