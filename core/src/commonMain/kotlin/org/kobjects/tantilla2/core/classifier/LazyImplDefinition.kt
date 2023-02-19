package org.kobjects.tantilla2.core.classifier

import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionDefinition

class LazyImplDefinition(
    parentScope: Scope,
    val traitName: String,
    val scopeName: String,
    docString: String,
) : ImplDefinition(parentScope, docString) {
    override val name: String
        get() = "$traitName for $scopeName"

    var resolvedTrait: TraitDefinition? = null
    var resolvedScope: Scope? = null

    override val trait: TraitDefinition
        get() {
            if (resolvedTrait == null) {
                resolvedTrait = parentScope.resolveStaticOrError(traitName, true).getValue(null) as TraitDefinition
            }
            return resolvedTrait!!
        }

    override val scope: Scope
        get() {
            if (resolvedScope == null) {
                resolvedScope = parentScope.resolveStaticOrError(scopeName, true).getValue(null) as Scope
            }
            return resolvedScope!!
        }

    override fun invalidate() {
        super.invalidate()
        resolvedScope = null
        resolvedTrait = null
    }
    init {
        userRootScope().unresolvedImpls.add(this)
    }

}