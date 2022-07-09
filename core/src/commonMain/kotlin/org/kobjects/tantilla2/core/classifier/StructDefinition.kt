package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.Parameter

open class StructDefinition(
    override val parentScope: Scope,
    override val name: String,
    override var docString: String,
) : Scope(), Type, Typed, Callable {
    override val kind: Definition.Kind
        get() = Definition.Kind.STRUCT

    override val type: FunctionType
        get() = StructMetaType(this, List<Parameter>(definitions.locals.size) {
            val name = definitions.locals[it]
            val def = definitions[name] as VariableDefinition
            Parameter(name, def.type, def.initializer()) })

    override val supportsMethods: Boolean
        get() = true

    override val supportsLocalVariables: Boolean
        get() = true

    override fun eval(context: RuntimeContext): Any? = context

    override fun serializeType(writer: CodeWriter) {
        writer.append(this.name)
    }

    override fun resolve(name: String) = resolveDynamic(name, false)

}