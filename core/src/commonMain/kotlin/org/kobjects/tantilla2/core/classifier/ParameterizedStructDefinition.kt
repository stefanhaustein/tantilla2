package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.scope.Scope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.type.Type

class ParameterizedStructDefinition(
    val unparameterized: Classifier,
    override val genericParameterTypes: List<Type>
) : Classifier(), Callable {
    override val type: FunctionType

    override fun eval(context: LocalRuntimeContext): Any {
        return (unparameterized as Callable).eval(context)
    }

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


    override fun unparameterized(): Classifier = unparameterized

    init {
        val map = getTypeMap()
        type = unparameterized.type.mapTypes{ map[it] ?: it } as FunctionType
    }


}