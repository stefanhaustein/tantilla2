package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.type.Type

class ParameterizedTraitMethodDefinition(
    newParent: TraitDefinition,
    unresolved: NativeTraitMethodDefinition,
    typeMapping: (Type) -> Type
) : NativeTraitMethodDefinition(
    newParent,
    unresolved.name,
    unresolved.docString,
    unresolved.type.mapTypes(typeMapping)
) {
}