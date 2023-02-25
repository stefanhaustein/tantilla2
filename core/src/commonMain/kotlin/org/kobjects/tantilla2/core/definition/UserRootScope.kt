package org.kobjects.tantilla2.core.definition

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.classifier.*

class UserRootScope(
    override val parentScope: SystemRootScope,
) : Scope(), ContextOwner, DocStringUpdatable {
    override var docString = ""
    val staticFieldDefinitions = mutableListOf<FieldDefinition?>()

    var classToTrait = mutableMapOf<Scope, MutableMap<TraitDefinition, ImplDefinition>>()
    var traitToClass = mutableMapOf<TraitDefinition, MutableMap<Scope, ImplDefinition>>()
    var definitionsWithErrors = mutableMapOf<Definition, List<Throwable>>()
    var definitionsWithChildErrors = mutableMapOf<Definition, MutableSet<Definition>>()

    val unresolved = mutableSetOf<Definition>()
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

    fun invalidateAll() {
        unresolvedImpls.addAll(classToTrait.values.flatMap { it.values })

        classToTrait.clear()
        traitToClass.clear()
        definitionsWithErrors.clear()

        staticFieldDefinitions.clear()

        recurse { it.invalidate() }

        for (definition in parentScope) {
            if (definition is NativeImplDefinition) {
                unresolvedImpls.add(definition)
            }
        }
        for (definition in AbsoluteRootScope) {
            if (definition is NativeImplDefinition) {
                unresolvedImpls.add(definition)
            }
        }
    }


    fun rebuild() {
        invalidateAll()
        // resolveAll()

        while (rebuildOne()) {

        }
        // return !childError && !localError
    }

    fun rebuildOne(): Boolean {
        val definition = unresolved.firstOrNull() ?: return false
        unresolved.remove(definition)
        try {
            definition.resolve()
            definitionsWithErrors.remove(definition)
            var parent = definition.parentScope
            while (parent != null && parent != AbsoluteRootScope) {
                if (definitionsWithChildErrors.contains(parent)) {
                    definitionsWithChildErrors.get(parent)!!.remove(definition)
                }
                parent = parent.parentScope
            }
        } catch (e: Exception) {
            e.printStackTrace()
            definitionsWithErrors[definition] = listOf(e)
            var parent = definition.parentScope
            while (parent != null && parent !is SystemRootScope) {
                definitionsWithChildErrors.getOrPut(parent) { mutableSetOf<Definition>() }.add(definition)
                parent = parent.parentScope
            }
        }
        return true
    }

    override val dynamicScopeSize: Int
        get() = staticFieldDefinitions.size

}