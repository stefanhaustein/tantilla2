package org.kobjects.tantilla2.core

class UserScope(parentScope: Scope) : Scope(parentScope) {
    override val name: String
        get() = "UserScope"
}