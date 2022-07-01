package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.classifier.UserClassDefinition

class CompilationResults {
    val errors = mutableSetOf<DefinitionImpl>()
    val traitToClass = mutableMapOf<TraitDefinition, MutableMap<UserClassDefinition, DefinitionImpl>>()
    val classToTrait = mutableMapOf<UserClassDefinition, MutableMap<TraitDefinition, DefinitionImpl>>()
}