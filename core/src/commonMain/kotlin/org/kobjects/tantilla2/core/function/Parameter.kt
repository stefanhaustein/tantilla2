package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.type.Type

data class Parameter(
    val name: String,
    val type: Type,
    // Inserted at callsite
    val defaultValueExpression: Node? = null,
    val isVararg: Boolean = false,
) {

    fun serialize(writer: CodeWriter) {
        if (name == "self") {
            writer.append(name)
        } else {
            if (isVararg) {
                writer.append("*")
            }

            if (writer.forTitle && !name.startsWith("$")) {
                writer.append(name)
            } else {
                if (!name.startsWith("$")) {
                    writer.append(name)
                    writer.append(": ")
                }
                writer.appendType(type)
                if (defaultValueExpression != null) {
                    writer.append(" = ")
                    writer.appendCode(defaultValueExpression)
                }
            }
        }
    }

    override fun toString(): String {
        val writer = CodeWriter(forTitle = true)
        serialize(writer)
        return writer.toString()
    }
}