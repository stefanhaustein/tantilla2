package org.kobjects.tantilla2.core.scope

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.serializeCode

abstract class AbstractFieldDefinition(
) : Definition {

    abstract var index: Int




    override fun getValue(self: Any?) = (self as LocalRuntimeContext)[index]

    override fun setValue(self: Any?, newValue: Any) {
        (self as LocalRuntimeContext).variables[index] = newValue
    }


    override fun isDynamic() = kind == Definition.Kind.PROPERTY

    override fun toString() = serializeCode()

}