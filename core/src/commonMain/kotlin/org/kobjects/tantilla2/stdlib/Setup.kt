package org.kobjects.tantilla2.stdlib

import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.runtime.RootScope

object StdLib {
    fun setup(rootScope: RootScope) {
        val penDefinition = PenDefinition(rootScope)
        rootScope.definitions["Pen"] = Definition(
            scope = rootScope,
            kind = Definition.Kind.CLASS,
            builtin = true,
            name = "Pen",
            explicitValue = penDefinition
        )
    }
}