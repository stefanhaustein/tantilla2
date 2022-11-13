package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.type.Type

class TraitDefinition(
    override val parentScope: Scope,
    override val name: String,
    override var docString: String,
) : Scope(), Type {

    override val supportsMethods: Boolean
        get() = true

    var traitIndex = 0

    override fun serializeType(writer: CodeWriter, scope: Scope?) {
        writer.append(scope?.typeName(this) ?: name)
    }

    override fun isAssignableFrom(type: Type): Boolean {
        return type == this || (type is ImplDefinition && type.trait == this)
    }

    override val kind: Definition.Kind
        get() = Definition.Kind.TRAIT



    override fun resolve(name: String) = resolveDynamic(name, false)
}