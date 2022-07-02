package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Lambda

class UserClassDefinition(
    override val name: String,
    parentScope: Scope,
) : Scope(parentScope), Type, Typed, Lambda {
    override val type: FunctionType
        get() = UserClassMetaType(this)

    override val supportsMethods: Boolean
        get() = true

    override val supportsLocalVariables: Boolean
        get() = true

    override fun eval(context: RuntimeContext) = context

    override fun serializeType(writer: CodeWriter) {
        writer.append(this.name)
    }

    override fun resolve(name: String) = resolveDynamic(name, false)

}