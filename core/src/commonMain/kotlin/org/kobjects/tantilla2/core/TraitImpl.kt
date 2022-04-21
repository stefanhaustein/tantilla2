package org.kobjects.tantilla2.core

class TraitImpl(
    name: String,
    parentContext: ParsingContext?,
    val trait: Trait,
    val classifier: ParsingContext,
) : ParsingContext(name, ParsingContext.Kind.IMPL, parentContext) {
    var vmt = listOf<Lambda>()

    override fun resolveAll() {
        trait.resolveAll()
        classifier.resolveAll()
        super.resolveAll()
    }

}