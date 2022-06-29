package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.classifier.UserClassDefinition

class CompilationResults {
    val errors = mutableSetOf<Definition>()
    val traitToClass = mutableMapOf<TraitDefinition, MutableMap<UserClassDefinition, Definition>>()
    val classToTrait = mutableMapOf<UserClassDefinition, MutableMap<TraitDefinition, Definition>>()
}