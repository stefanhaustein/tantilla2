package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.classifier.StructDefinition

class CompilationResults {
    val errors = mutableSetOf<Definition>()
    val traitToClass = mutableMapOf<TraitDefinition, MutableMap<StructDefinition, Definition>>()
    val classToTrait = mutableMapOf<StructDefinition, MutableMap<TraitDefinition, Definition>>()
}