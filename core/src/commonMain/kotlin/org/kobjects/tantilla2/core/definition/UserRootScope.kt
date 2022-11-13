package org.kobjects.tantilla2.core.definition

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.CompilationResults
import org.kobjects.tantilla2.core.GlobalRuntimeContext
import org.kobjects.tantilla2.core.type.RootScope
import org.kobjects.tantilla2.core.classifier.FieldDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition

class UserRootScope(
    override val parentScope: RootScope,
) : Scope() {
    override var docString = ""
    val staticFieldDefinitions = mutableListOf<FieldDefinition?>()

    var initializedTo = 0
    var classToTrait = emptyMap<Scope, MutableMap<TraitDefinition, Definition>>()
    var traitToClass = emptyMap<TraitDefinition, MutableMap<Scope, Definition>>()
    var definitionsWithErrors = emptySet<Definition>()

    override val kind: Definition.Kind
        get() = Definition.Kind.UNIT

    override val name: String
        get() = "<UserScope>"

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        serializeBody(writer)
    }

    override fun registerStatic(fieldDefinition: FieldDefinition): Int {
        staticFieldDefinitions.add(fieldDefinition)
        return staticFieldDefinitions.size - 1
    }

    override fun reset() {
        super.reset()
        staticFieldDefinitions.clear()
        initializedTo = 0
    }

    fun initialize(globalRuntimeContext: GlobalRuntimeContext, incremental: Boolean = false) {
        val startIndex = if (incremental) initializedTo else 0
        globalRuntimeContext.staticVariableValues.setSize(staticFieldDefinitions.size)
        for (index in startIndex until staticFieldDefinitions.size) {
            staticFieldDefinitions[index]?.initialize(globalRuntimeContext.staticVariableValues)
        }
        initializedTo = staticFieldDefinitions.size
    }


    fun rebuild() {
        reset()
        val results = CompilationResults()
        resolveAll(results)

        classToTrait = results.classToTrait.toMap()
        traitToClass = results.traitToClass.toMap()
        definitionsWithErrors = results.definitionsWithErrors.toSet()
    }

}