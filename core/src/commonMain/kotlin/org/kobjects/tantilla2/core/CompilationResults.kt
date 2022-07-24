package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.classifier.StructDefinition

class CompilationResults {
    val traitToClass = mutableMapOf<TraitDefinition, MutableMap<StructDefinition, Definition>>()
    val classToTrait = mutableMapOf<StructDefinition, MutableMap<TraitDefinition, Definition>>()

    val definitionCompilationResults = mutableMapOf<Definition, DefinitionCompilationResult>()


    class DefinitionCompilationResult(
        val definition: Definition,
        val errors: List<Exception>,
        val errorsInChildren: Boolean
    ) {
        val errorOrChildError: Boolean
            get() = errors.isNotEmpty() || errorsInChildren
    }

}