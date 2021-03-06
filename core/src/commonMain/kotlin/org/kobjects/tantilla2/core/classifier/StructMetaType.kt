package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter

class StructMetaType (
    val wrapped: StructDefinition,
    ctorParams: List<Parameter>
) : FunctionType.Impl(
    wrapped,
    ctorParams
) {
    override fun resolve(name: String) = wrapped.resolveStatic(name, false)


}