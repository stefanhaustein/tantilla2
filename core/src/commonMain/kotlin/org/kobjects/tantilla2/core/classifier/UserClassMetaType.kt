package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter

class UserClassMetaType(
    val wrapped: UserClassDefinition,
) : FunctionType.Impl(
    wrapped,
    List<Parameter>(wrapped.definitions.locals.size) {
        val name = wrapped.definitions.locals[it]
        val def = wrapped.definitions[name]!!
        Parameter(name, def.valueType(), def.initializer()) }
) {
    override fun resolve(name: String) = wrapped.resolveStatic(name, false)


}