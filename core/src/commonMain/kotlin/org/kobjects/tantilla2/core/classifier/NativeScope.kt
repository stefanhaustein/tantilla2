package org.kobjects.tantilla2.core.classifier

import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunction
import org.kobjects.tantilla2.core.function.Parameter

open class NativeScope(var name: String, parent: Scope) : Scope(parent), SerializableType {
    override val title: String
        get() = name


    override fun serializeType(writer: CodeWriter) {
        writer.append(name)
    }

}