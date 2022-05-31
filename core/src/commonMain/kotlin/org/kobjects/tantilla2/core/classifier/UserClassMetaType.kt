package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter

class UserClassMetaType(
    val wrapped: UserClassDefinition,
) : FunctionType.Impl(
    wrapped,
    List<Parameter>(wrapped.locals.size) {
        val name = wrapped.locals[it]
        val def = wrapped[name]!!
        Parameter(name, def.type(), def.initializer()) }
) {
    override fun resolve(name: String): Definition = wrapped.resolveStatic(name, false)


}