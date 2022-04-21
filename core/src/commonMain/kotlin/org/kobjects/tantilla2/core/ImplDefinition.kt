package org.kobjects.tantilla2.core

class ImplDefinition(
    name: String,
    parentContext: Scope?,
    val trait: TraitDefinition,
    val classifier: Scope,
) : Scope(name, Scope.Kind.IMPL, parentContext) {
    var vmt = listOf<Callable>()

    override fun resolveAll() {
        trait.resolveAll()
        classifier.resolveAll()
        super.resolveAll()
    }

}