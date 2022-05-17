package org.kobjects.tantilla2.core

class UserScope(parentScope: Scope) : Scope(parentScope) {
    override val title: String
        get() = "UserScope"
}