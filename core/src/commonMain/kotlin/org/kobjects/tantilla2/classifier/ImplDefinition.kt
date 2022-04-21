package org.kobjects.tantilla2.classifier

import org.kobjects.tantilla2.function.Callable
import org.kobjects.tantilla2.core.Scope

class ImplDefinition(
    name: String,
    parentContext: Scope?,
    val trait: TraitDefinition,
    val classifier: Scope,
) : Scope(name, parentContext) {
    var vmt = listOf<Callable>()

    override fun resolveAll() {
        trait.resolveAll()
        classifier.resolveAll()
        super.resolveAll()
    }

}