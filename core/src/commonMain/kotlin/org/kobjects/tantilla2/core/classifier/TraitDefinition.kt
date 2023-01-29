package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.type.ScopeType
import org.kobjects.tantilla2.core.type.Type

class TraitDefinition(
    override val parentScope: Scope,
    override val name: String,
    override var docString: String,
) : Classifier() {

    var traitIndex = 0


    override fun isAssignableFrom(type: Type, allowAs: Boolean): Boolean {
        if (type == this || (type is ImplDefinition && type.trait == this)) {
            return true
        }
        if (allowAs && getImplementationForTypeOrNull(type) != null) {
            return true
        }
        return false
    }

    override val kind: Definition.Kind
        get() = Definition.Kind.TRAIT


    fun requireImplementationFor(type: Type): ImplDefinition =
        getImplementationForTypeOrNull(type) ?: throw IllegalStateException("$typeName for ${type.typeName} not found.")

    fun getImplementationForTypeOrNull(type: Type): ImplDefinition? {
        val userRootScope = userRootScope()
        val unresolvedImpls = userRootScope.unresolvedImpls.toList()

        for (impl in unresolvedImpls) {
            try {
                userRootScope.classToTrait
                    .getOrPut(impl.scope) { mutableMapOf() }[impl.trait] = impl
                userRootScope.traitToClass
                    .getOrPut(impl.trait) { mutableMapOf() }[impl.scope] = impl
                userRootScope.unresolvedImpls.remove(impl)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // TODO: Figure out how to fix this properly.
        val scope = if (type is Scope) type else (type as ScopeType).scope

        return userRootScope.traitToClass[this]?.get(scope)
    }
}