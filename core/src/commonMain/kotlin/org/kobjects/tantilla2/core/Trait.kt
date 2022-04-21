package org.kobjects.tantilla2.core

class Trait(
    name: String,
    parent: ParsingContext,
) : ParsingContext(name, ParsingContext.Kind.TRAIT, parent) {
    var traitIndex = 0
}