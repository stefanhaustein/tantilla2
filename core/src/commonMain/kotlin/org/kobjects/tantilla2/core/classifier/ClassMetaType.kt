package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter

class ClassMetaType(
    val wrapped: ClassDefinition,
) : FunctionType(
    wrapped,
    List<Parameter>(wrapped.locals.size) {
        val name = wrapped.locals[it]
        val def = wrapped.definitions[name]!!
        Parameter(name, def.type()) }
)