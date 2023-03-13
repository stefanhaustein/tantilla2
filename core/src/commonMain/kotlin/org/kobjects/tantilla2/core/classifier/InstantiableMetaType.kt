package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.Type

class InstantiableMetaType (
    val wrapped: Classifier,
    ctorParams: List<Parameter>
) : FunctionType.Impl(
    wrapped,
    ctorParams
) {
    override fun resolve(name: String) = wrapped.resolveStatic(name, false)

    override fun mapTypes(mapping: (Type) -> Type) = InstantiableMetaType(wrapped.mapTypes(mapping) as Classifier, parameters.map{
        Parameter(it.name, it.type.mapTypes(mapping), it.defaultValueExpression, it.isVararg)
    })
}