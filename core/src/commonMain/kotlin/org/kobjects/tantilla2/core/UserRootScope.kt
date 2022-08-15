package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.classifier.FieldDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType

class UserRootScope(
    override val parentScope: Scope,
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

    override var docString: String
        get() = ""
        set(_) = throw UnsupportedOperationException()

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

    fun run(globalRuntimeContext: GlobalRuntimeContext) {
        val definition = this["main"]
            ?: throw RuntimeException("main() undefined.")
        if (definition.type !is FunctionType) {
            throw RuntimeException("main is not a function.")
        }
        val function = definition.getValue(null) as Callable
        initialize(globalRuntimeContext)
        function.eval(LocalRuntimeContext(globalRuntimeContext, function.scopeSize))
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