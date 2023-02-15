package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.TraitMethodBody
import org.kobjects.tantilla2.core.classifier.LazyImplDefinition
import org.kobjects.tantilla2.core.classifier.NativeImplDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition.Companion.vmtIndex
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionDefinition

class IterableImpl(
    parentScope: Scope,
    trait: IterableTrait,
    scope: Scope,
    docString: String
) : NativeImplDefinition(parentScope, trait, scope, docString)  {

  /*  init {
        val vmt = Array<Callable?>(trait.traitIndex) { null }
        for (definition in trait) {
            val index =  definition.vmtIndex
            val resolved = scope[definition.name]
                ?: throw RuntimeException("Can't resolve '${definition.name}' for '${this.name}'")
            vmt[index] = resolved.getValue(null) as Callable
        }
        this.vmt = vmt.toList() as List<Callable>
    }*/

    init {
        resolve()
    }

}