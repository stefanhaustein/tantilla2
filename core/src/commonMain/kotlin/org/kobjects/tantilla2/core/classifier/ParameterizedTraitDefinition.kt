package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.type.Type

class ParameterizedTraitDefinition(
    val unparameterized: TraitDefinition,
    genericParameterTypes: List<Type>,
) : TraitDefinition(unparameterized.parentScope, unparameterized.name, unparameterized.docString, genericParameterTypes) {


    override fun unparameterized(): TraitDefinition = unparameterized

}