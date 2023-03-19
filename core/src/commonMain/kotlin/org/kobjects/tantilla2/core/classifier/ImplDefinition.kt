package org.kobjects.tantilla2.core.classifier

import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.scope.Scope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.type.Type

abstract class ImplDefinition(
    override val parentScope: Scope,
    override var docString: String,
) : Classifier() {

    abstract val trait: TraitDefinition

    abstract val scope: Scope

    var vmt = emptyList<Callable>()

    override val supportsMethods: Boolean
        get() = true

    override fun invalidate() {
        super.invalidate()
        vmt = emptyList()
    }


    override fun resolve(applyOffset: Boolean, errorCollector: MutableList<ParsingException>?) {

        vmt = trait.createVmt { traitMethod ->
            var resolved = resolve(traitMethod.name)
            if (resolved == null && scope is Type) {
                resolved = (scope as Type).resolve(traitMethod.name)
            }
            if (resolved == null) { throw IllegalArgumentException("No implementation found for trait method '${traitMethod.name}' in '$name'") }

            val resolvedMethod = resolved.getValue(null) as Callable

            resolvedMethod.type.requireTraitMethodTypeMatch(name, traitMethod.type as FunctionType)

            resolvedMethod
        }
    }

    override val kind: Definition.Kind
        get() = Definition.Kind.IMPL


    fun forType(type: Type): ImplDefinition {
        if (scope !is Type) {
            return this
        }
        val scopeType = scope as Type
        val map = mutableMapOf<Type, Type>()
        for (i in type.genericParameterTypes.indices) {
             map[scopeType.genericParameterTypes[i]] = type.genericParameterTypes[i]
        }

        println("Impl type map: $map")

        val resolvedTrait = trait.mapTypes { map[it] ?: it } as TraitDefinition

        val resolvedImpl = NativeImplDefinition(parentScope, resolvedTrait, scope, docString)

        resolvedImpl.vmt = vmt
        /*
        for (def in this) {
            resolvedImpl.add(def.withTypesMapped(resolvedImpl) { map[it] ?: it })
        }*/

        return resolvedImpl
    }

}