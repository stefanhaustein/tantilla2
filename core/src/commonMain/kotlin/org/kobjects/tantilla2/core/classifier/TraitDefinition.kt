package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.SerializableType

class TraitDefinition(
    val name: String,
    parent: Scope,
) : Scope(parent), SerializableType {

    override val title: String
        get() = name

    var traitIndex = 0

    override fun serializeType(writer: CodeWriter) {
        writer.append(name)
    }

}