package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionDefinition

class ImplDefinition(
    override val parentScope: Scope,
    val traitName: String,
    val scopeName: String,
    override var docString: String,
) : Classifier() {
    override val name: String
        get() = "$traitName for $scopeName"

    var vmt = emptyList<Callable>()

    var resolvedTrait: TraitDefinition? = null
    var resolvedScope: Scope? = null

    val trait: TraitDefinition
        get() {
            if (resolvedTrait == null) {
                resolvedTrait = parentScope.resolveStaticOrError(traitName, true).getValue(null) as TraitDefinition
            }
            return resolvedTrait!!
        }

    val scope: Scope
        get() {
            if (resolvedScope == null) {
                resolvedScope = parentScope.resolveStaticOrError(scopeName, true).getValue(null) as Scope
            }
            return resolvedScope!!
        }

    override val supportsMethods: Boolean
        get() = true

    override fun invalidate() {
        super.invalidate()
        resolvedScope = null
        resolvedTrait = null
        vmt = emptyList()
    }


    override fun resolve() {
        // TODO: Move VMT creation to trait?
        val vmt = Array<Callable?>(trait.traitIndex) { null }
        for (definition in trait) {
            val index = ((definition as FunctionDefinition).body() as TraitMethodBody).index
            val resolved = resolve(definition.name)
                ?: throw RuntimeException("Can't resolve '${definition.name}' for '${this.name}'")
            vmt[index] = resolved.getValue(null) as Callable
        }
        this.vmt = vmt.toList() as List<Callable>
    }

    override val kind: Definition.Kind
        get() = Definition.Kind.IMPL


    init {
        userRootScope().unresolvedImpls.add(this)
    }

}