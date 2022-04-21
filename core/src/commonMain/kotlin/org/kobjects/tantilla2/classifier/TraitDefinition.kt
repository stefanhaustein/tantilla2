package org.kobjects.tantilla2.classifier

import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.Scope

class TraitDefinition(
    override val name: String,
    parent: Scope,
) : Scope(parent), Type {
    var traitIndex = 0
}