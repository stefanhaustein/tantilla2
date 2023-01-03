package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.type.Type

/**
 * A typed scope that has some form of instances.
 */
abstract class Classifier : Scope(), Type {


    override fun serializeType(writer: CodeWriter) {
        serializeQualifiedName(writer, false)
    }

    override val supportsMethods: Boolean
        get() = true

    override fun resolve(name: String) = resolveDynamic(name, false)
}