package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter

object UnresolvedType : FunctionType {
    override val returnType: Type
        get() = UnresolvedType

    override val parameters: List<Parameter>
        get() = emptyList()

    override fun serializeType(writer: CodeWriter) {
        writer.append("<Unresolved Type>")
    }
}