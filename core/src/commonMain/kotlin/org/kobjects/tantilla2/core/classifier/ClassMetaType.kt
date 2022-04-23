package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.Scope

class ClassMetaType(
    val wrapped: ClassDefinition,
) : FunctionType(
    wrapped,
    List<Parameter>(wrapped.locals.size) {
        Parameter(wrapped.locals[it].name, wrapped.locals[it].type()) }
)