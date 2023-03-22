package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.classifier.NativeImplDefinition
import org.kobjects.tantilla2.core.scope.AbsoluteRootScope
import org.kobjects.tantilla2.core.scope.Scope
import org.kobjects.tantilla2.core.type.Type

class IterableImpl(
    parentScope: Scope,
    scope: Scope,
    elementType: Type,
    docString: String
) : NativeImplDefinition(parentScope, AbsoluteRootScope.iterableTrait.withGenericsResolved(listOf(elementType)), scope, docString)  {

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
        // TODO: Is this needed here? Seems to cause trouble with registration
        resolve()
    }

}