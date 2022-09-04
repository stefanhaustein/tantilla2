package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.classifier.FieldDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType

class UserRootScope(
    override val parentScope: Scope,
    override var docString: String = "",
) : Scope() {
    val staticFields = mutableListOf<FieldDefinition?>()

    var initializedTo = 0
    var classToTrait = emptyMap<Scope, MutableMap<TraitDefinition, Definition>>()
    var traitToClass = emptyMap<TraitDefinition, MutableMap<Scope, Definition>>()
    var definitionsWithErrors = emptySet<Definition>()

    override val kind: Definition.Kind
        get() = Definition.Kind.UNIT

    override val name: String
        get() = "<UserScope>"

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        serializeBody(writer)
    }

    override fun registerStatic(fieldDefinition: FieldDefinition) {
        staticFields.add(fieldDefinition)
    }

    override fun reset() {
        super.reset()
        staticFields.clear()
        initializedTo = 0
    }

    fun initialize(globalRuntimeContext: GlobalRuntimeContext, incremental: Boolean = false) {
        val startIndex = if (incremental) initializedTo else 0
        for (index in startIndex until staticFields.size) {
            staticFields[index]?.initialize(globalRuntimeContext)
        }
        initializedTo = staticFields.size
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