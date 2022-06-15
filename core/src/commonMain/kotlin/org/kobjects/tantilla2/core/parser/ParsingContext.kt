package org.kobjects.tantilla2.core.parser

import org.kobjects.tantilla2.core.Scope

data class ParsingContext(
    val scope: Scope,
    val depth: Int
) {
}