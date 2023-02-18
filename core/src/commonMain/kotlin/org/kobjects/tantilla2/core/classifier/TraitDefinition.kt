package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.TraitMethodBody
import org.kobjects.tantilla2.core.definition.ContextOwner
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.definition.UserRootScope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionDefinition
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.NoneType
import org.kobjects.tantilla2.core.type.ScopeType
import org.kobjects.tantilla2.core.type.Type

open class TraitDefinition(
    override val parentScope: Scope?,
    override val name: String,
    override var docString: String,
    override val genericParameterTypes: List<Type> = listOf(),
) : Classifier() {

    // The current vmt index. Determines the vmt size
    var traitIndex = 0


    override fun isAssignableFrom(type: Type): Boolean {
        return (type == this || (type is ImplDefinition && type.trait == this))
    }

    override val kind: Definition.Kind
        get() = Definition.Kind.TRAIT


    fun defineMethod(
        name: String,
        docString: String,
        returnType: Type,
        vararg parameters: Parameter
    ): TraitDefinition {
        add(NativeTraitMethodDefinition(this, name, docString, FunctionType.Impl(returnType, listOf(Parameter("self", this)) + listOf(*parameters))))
        return this
    }

    fun requireImplementationFor(userRootScope: UserRootScope, type: Type): ImplDefinition =
        getImplementationForTypeOrNull(userRootScope, type) ?: throw IllegalStateException("$typeName for ${type.typeName} not found.")

    fun getImplementationForTypeOrNull(userRootScope: UserRootScope, type: Type): ImplDefinition? {
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

        // TODO: Figure out how to fix this properly for the static case
        val scope = if (type is Scope) type.unparameterized ?: type else (type as ScopeType).scope

        val impl = userRootScope.traitToClass[this.unparameterized ?: this]?.get(scope)

        return impl
    }

    fun createVmt(resolveMethod: (Definition) -> Callable): List<Callable> {
        val vmt = Array<Callable?>(traitIndex) { null }
        for (traitMethod in this) {
            val vmtIndex = traitMethod.vmtIndex
            vmt[vmtIndex] = resolveMethod(traitMethod)

        }
        return vmt.toList() as List<Callable>
    }


    companion object {


        fun Type.isConvertibleFrom(type: Type, userRootScope: UserRootScope): Boolean {
            if (isAssignableFrom(type)) {
                return true
            }
            if (this is TraitDefinition) {
                return getImplementationForTypeOrNull(userRootScope, type) != null
            }
            return false
        }

        val Definition.vmtIndex: Int
            get() = if (this is NativeTraitMethodDefinition) this.vmtIndex else
                ((this as FunctionDefinition).body() as TraitMethodBody).vmtIndex



    }




}