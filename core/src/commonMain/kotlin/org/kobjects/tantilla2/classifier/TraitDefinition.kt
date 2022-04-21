package org.kobjects.tantilla2.classifier

import org.kobjects.tantilla2.core.Scope

class TraitDefinition(
    name: String,
    parent: Scope,
) : Scope(name, parent) {
    var traitIndex = 0
}