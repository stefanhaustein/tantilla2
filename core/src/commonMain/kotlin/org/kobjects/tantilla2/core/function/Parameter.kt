package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.node.Evaluable
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.type.Type

data class Parameter(
    val name: String,
    val type: Type,
    val defaultValueExpression: Evaluable? = null,
    val isVararg: Boolean = false,
) {

    fun serialize(writer: CodeWriter, scope: Scope?) {
        if (name == "self") {
            writer.append(name)
        } else {
            if (isVararg) {
                writer.append("*")
            }
            writer.append(name)
            if (scope != null) {
                writer.append(": ")
                writer.appendType(type, scope)
                if (defaultValueExpression != null) {
                    writer.append(" = ")
                    writer.appendCode(defaultValueExpression)
                }
            }
        }
    }

    override fun toString(): String {
        val writer = CodeWriter()
        serialize(writer, null)
        return writer.toString()
    }
}