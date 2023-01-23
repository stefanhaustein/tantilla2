package org.kobjects.tantilla2.core.parser

import org.kobjects.tantilla2.core.definition.Scope

data class ParsingContext(
    val scope: Scope,
    val depth: Int
) {
    fun indent() = ParsingContext(scope, depth + 1)

}