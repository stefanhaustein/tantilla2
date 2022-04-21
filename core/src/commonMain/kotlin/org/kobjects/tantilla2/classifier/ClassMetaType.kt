package org.kobjects.tantilla2.classifier

import org.kobjects.tantilla2.function.FunctionType
import org.kobjects.tantilla2.function.Parameter
import org.kobjects.tantilla2.core.Scope

class ClassMetaType(
    val wrapped: Scope,
) : FunctionType(
    wrapped,
    List<Parameter>(wrapped.locals.size) {
        Parameter(wrapped.locals[it].name, wrapped.locals[it].type()) }
)