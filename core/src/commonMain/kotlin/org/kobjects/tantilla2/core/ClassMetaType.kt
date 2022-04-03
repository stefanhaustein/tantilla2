package org.kobjects.tantilla2.core

class ClassMetaType(
    val wrapped: ParsingContext,
) : FunctionType(
    false,
    wrapped,
    List<Parameter>(wrapped.locals.size) {
        Parameter(wrapped.locals[it].name, wrapped.locals[it].type(wrapped)) }
)