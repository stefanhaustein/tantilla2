package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*

open class NativeScope(var name: String, parent: Scope) : Scope(parent), SerializableType {
    override val title: String
        get() = name


    override fun serializeType(writer: CodeWriter) {
        writer.append(name)
    }

}