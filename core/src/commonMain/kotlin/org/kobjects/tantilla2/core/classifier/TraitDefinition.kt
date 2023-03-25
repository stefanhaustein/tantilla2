package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.TraitMethodBody
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.scope.Scope
import org.kobjects.tantilla2.core.scope.UserRootScope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionDefinition
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.scope.ScopeType
import org.kobjects.tantilla2.core.type.*

open class TraitDefinition(
    override val parentScope: Scope?,
    override val name: String,
    override var docString: String,
    override val genericParameterTypes: List<Type> = listOf(),
) : Classifier() {

    // The current vmt index.
    var nextTraitIndex = 0

    // Accesses definitions to make sure lazy resolution is triggered and the value is up-to-date
    val vmtSize
        get() = definitions.size + nextTraitIndex - definitions.size

    override fun withGenericsResolved(resolved: List<Type>): TraitDefinition {
        return ParameterizedTraitDefinition(unparameterized() as TraitDefinition, resolved)
    }


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
        getImplementationForTypeOrNull(userRootScope, type) ?: throw IllegalStateException("$typeName for ${type.typeName} not found; available: ${userRootScope.traitToClass}.")

    fun getImplementationForTypeOrNull(userRootScope: UserRootScope, type: Type): ImplDefinition? {
        val unresolvedImpls = userRootScope.unresolvedImpls.toList()

        for (impl in unresolvedImpls) {
            try {
                userRootScope.classToTrait
                    .getOrPut(impl.scope) { mutableMapOf() }[impl.trait] = impl
                userRootScope.traitToClass
                    .getOrPut(impl.trait.unparameterized() as TraitDefinition) { mutableMapOf() }[impl.scope] = impl
                userRootScope.unresolvedImpls.remove(impl)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // TODO: Figure out how to fix this properly for the static case
        val unparameterizedType = if (type is Scope) type.unparameterized() else (type as ScopeType).scope

        val allImplsForTrait = userRootScope.traitToClass[unparameterized()]
        val impl = allImplsForTrait?.get(unparameterizedType)

        println("unparameterized type: $unparameterizedType; trait:$this unparameterized trait: ${unparameterized()} Impls for trait: $allImplsForTrait; impl found: $impl")

        return impl?.forType(type)
    }

    fun createVmt(resolveMethod: (Definition) -> Callable): List<Callable> {

        val vmt = Array<Callable?>(vmtSize) { null }
        for (traitMethod in this.filter { it.isDynamic() }) {
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


    override fun resolveGenericsImpl(
        actualType: Type,
        map: GenericTypeMap,
        allowNoneMatch: Boolean,
        allowAs: UserRootScope?
    ): Type {
        if (allowAs != null) {
            val impl = getImplementationForTypeOrNull(allowAs, actualType)
            if (impl != null) {
                val actualTrait = impl.trait
                val resolvedParameters = List<Type>(genericParameterTypes.size) {
                    if (actualTrait.genericParameterTypes[it] !is TypeVariable
                        && actualTrait.genericParameterTypes[it] !is TypeParameter)
                        actualTrait.genericParameterTypes[it] else genericParameterTypes[it]
                }
                return withGenericsResolved(resolvedParameters)
            }
        }
        return super.resolveGenericsImpl(actualType, map, allowNoneMatch, allowAs)
    }

}