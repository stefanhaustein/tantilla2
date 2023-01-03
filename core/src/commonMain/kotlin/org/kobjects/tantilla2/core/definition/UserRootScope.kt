package org.kobjects.tantilla2.core.definition

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.classifier.FieldDefinition
import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition

class UserRootScope(
    override val parentScope: SystemRootScope,
) : Scope(), DynamicScope {
    override var docString = ""
    val staticFieldDefinitions = mutableListOf<FieldDefinition?>()

    var initializedTo = 0
    var classToTrait = mutableMapOf<Scope, MutableMap<TraitDefinition, ImplDefinition>>()
    var traitToClass = mutableMapOf<TraitDefinition, MutableMap<Scope, ImplDefinition>>()
    var definitionsWithErrors = mutableMapOf<Definition, List<Throwable>>()

    val unresolvedImpls = mutableSetOf<ImplDefinition>()

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

        unresolvedImpls.addAll(classToTrait.values.flatMap { it.values })

        classToTrait.clear()
        traitToClass.clear()
        definitionsWithErrors.clear()

        staticFieldDefinitions.clear()
        initializedTo = 0
    }


    fun rebuild() {
        reset()
        resolveAll()
    }

    override val dynamicScopeSize: Int
        get() = staticFieldDefinitions.size

}