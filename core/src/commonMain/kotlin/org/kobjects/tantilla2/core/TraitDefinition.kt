package org.kobjects.tantilla2.core

class TraitDefinition(
    name: String,
    parent: Scope,
) : Scope(name, Scope.Kind.TRAIT, parent) {
    var traitIndex = 0
}