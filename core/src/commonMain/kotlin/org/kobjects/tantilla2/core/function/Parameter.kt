package org.kobjects.tantilla2.core.function

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.SerializableCode
import org.kobjects.tantilla2.core.Type

data class Parameter(
    val name: String,
    val type: Type,
    val defaultValueExpression: Evaluable<RuntimeContext>? = null,
    val isVararg: Boolean = false,
): SerializableCode {

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        if (name == "self") {
            writer.append(name)
        } else {
            if (isVararg) {
                writer.append("*")
            }
            writer.append(name)
            writer.append(": ")
            writer.appendType(type)
            if (defaultValueExpression != null) {
                writer.append(" = ")
                writer.appendCode(defaultValueExpression)
            }
        }
    }

    override fun toString() = CodeWriter().appendCode(this).toString()
}