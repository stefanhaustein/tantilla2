package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.classifier.StructDefinition

class CompilationResults {
    val traitToClass = mutableMapOf<TraitDefinition, MutableMap<Scope, Definition>>()
    val classToTrait = mutableMapOf<Scope, MutableMap<TraitDefinition, Definition>>()

    val definitionsWithErrors = mutableSetOf<Definition>()

}