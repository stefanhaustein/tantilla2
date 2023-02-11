package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.classifier.LazyImplDefinition
import org.kobjects.tantilla2.core.classifier.NativeImplDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.definition.Scope

class IterableImpl(
    parentScope: Scope,
    trait: TraitDefinition,
    scope: Scope,
    docString: String
) : NativeImplDefinition(parentScope, trait, scope, docString)  {

    init {
  //      defineMethod("iterator", )
    }

}