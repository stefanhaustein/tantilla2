package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.Type

class TraitDefinition(
    val name: String,
    parent: Scope,
) : Scope(parent), Type {

    override val title: String
        get() = name

    override val supportsMethods: Boolean
        get() = true

    var traitIndex = 0

    override fun serializeType(writer: CodeWriter) {
        writer.append(name)
    }

    override fun isAssignableFrom(type: Type): Boolean {
        return type == this || (type is ImplDefinition && type.trait == this)
    }

    override fun resolve(name: String) = resolveDynamic(name, false)
}