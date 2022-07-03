package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Lambda

class UserClassDefinition(
    parentScope: Scope,
    override val name: String,
    definitionText: String,
    override var docString: String,
) : Scope(parentScope, definitionText), Type, Typed, Lambda {
    override val kind: Definition.Kind
        get() = Definition.Kind.STRUCT

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