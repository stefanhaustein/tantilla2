package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.TraitMethodBody
import org.kobjects.tantilla2.core.classifier.LazyImplDefinition
import org.kobjects.tantilla2.core.classifier.NativeImplDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition.Companion.vmtIndex
import org.kobjects.tantilla2.core.definition.AbsoluteRootScope
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.definition.SystemRootScope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionDefinition
import org.kobjects.tantilla2.core.type.Type

class IterableImpl(
    parentScope: Scope,
    scope: Scope,
    elementType: Type,
    docString: String
) : NativeImplDefinition(parentScope, AbsoluteRootScope.iterableTrait.withElementType(elementType), scope, docString)  {

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