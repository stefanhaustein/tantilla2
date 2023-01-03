package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.Typed

open class StructDefinition(
    override val parentScope: Scope?,
    override val name: String,
    override var docString: String,
) : Classifier(), Typed, Callable {
    override val kind: Definition.Kind
        get() = Definition.Kind.STRUCT

    override val type: InstantiableMetaType
        get() = InstantiableMetaType(this, List<Parameter>(locals.size) {
            val name = locals[it]
            val def = this[name] as FieldDefinition
            Parameter(name, def.type, def.initializer()) })


    override val supportsLocalVariables: Boolean
        get() = true

    override fun eval(context: LocalRuntimeContext): Any = context



}