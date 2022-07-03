package org.kobjects.tantilla2.core

class UserScope(
    parentScope: Scope,
) : Scope(parentScope) {
    override val kind: Definition.Kind
        get() = Definition.Kind.UNIT

    override val name: String
        get() = "UserScope"

    override var docString: String
        get() = ""
        set(_) = throw UnsupportedOperationException()
}