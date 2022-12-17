package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.GenericType
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.Typed

open class StructDefinition(
    override val parentScope: Scope?,
    override val name: String,
    override var docString: String,
) : Scope(), Type, Typed, Callable {
    override val kind: Definition.Kind
        get() = Definition.Kind.STRUCT

    override val type: StructMetaType
        get() = StructMetaType(this, List<Parameter>(locals.size) {
            val name = locals[it]
            val def = this[name] as FieldDefinition
            Parameter(name, def.type, def.initializer()) })

    override val supportsMethods: Boolean
        get() = true

    override val supportsLocalVariables: Boolean
        get() = true

    override fun eval(context: LocalRuntimeContext): Any = context

    override fun serializeType(writer: CodeWriter, scope: Scope?) {
        writer.append(scope?.typeName(this) ?: name)
        if (this is GenericType) {
            writer.append('[')
            val types = genericParameterTypes
            types.first().serializeType(writer, scope)
            for (i in 1 until genericParameterTypes.size) {
                writer.append(", ")
                types[i].serializeType(writer, scope)
            }
            writer.append(']')
        }
    }

    override fun resolve(name: String) = resolveDynamic(name, false)

}