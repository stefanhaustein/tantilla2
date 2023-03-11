package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.type.Type

class ResolvedGenericType(
    override val unparameterized: Classifier,
    override val genericParameterTypes: List<Type>
) : Classifier() {
    override val parentScope: Scope?
        get() = unparameterized.parentScope

    override val kind: Definition.Kind
        get() = unparameterized.kind

    override val name: String
        get() = unparameterized.name

    override var docString: String
        get() = unparameterized.docString
        set(value) {
            throw UnsupportedOperationException()
        }

    init {
        val map = mutableMapOf<Type, Type>()
        for (i in unparameterized.genericParameterTypes.indices) {
            map[unparameterized.genericParameterTypes[i]] = genericParameterTypes[i]
        }

        for (member in unparameterized) {
            add(member.withTypesMapped { map[it] ?: it })
        }
    }


}