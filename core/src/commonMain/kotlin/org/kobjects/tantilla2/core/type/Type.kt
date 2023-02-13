package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.classifier.TraitDefinition.Companion.isConvertibleFrom
import org.kobjects.tantilla2.core.definition.Definition


/**
 * This is not a sublcass of Scope because for FunctionType a Scope seems like too much overhead.
 * It probably makes sense to remove resolve here -- or map it to the corresponding Scope
 * concept directly.
 */

interface Type {

    fun isAssignableFrom(type: Type) = type == this

    fun serializeType(writer: CodeWriter)

    // Something that can easily be used in toString()
    val typeName: String
        get() = CodeWriter().appendType(this).toString()

    fun resolve(name: String): Definition? = throw UnsupportedOperationException("Can't resolve '$name' for '$typeName'")

    val genericParameterTypes: List<Type>
        get() = emptyList()

    fun withGenericsResolved(genericTypeMap: GenericTypeMap): Type =
        throw UnsupportedOperationException()

    val unparameterized: Type?
        get() = null
    
    fun serializeGenerics(writer: CodeWriter) {
        val generics = genericParameterTypes
        if (generics.isNotEmpty()) {
            writer.append('[')
            writer.appendType(generics[0])
            for (i in 1 until generics.size) {
                writer.append(", ")
                writer.appendType(generics[i])
            }
            writer.append(']')
        }
    }

    /** Resolve generics in this type. Actual is the type implied by code, if available */
    fun resolveGenerics(
        actualType: Type?,
        map: GenericTypeMap,
        allowNoneMatch: Boolean = false, //
        allowAs: Boolean = false,
    ): Type {
        if (actualType != this && actualType != null) {
            if (allowAs && isConvertibleFrom(actualType, map.userRootScope)) {
                return this
            }
            throw IllegalArgumentException("Type mismatch. Expected: $this actual: $actualType")
        }
        return this
    }

}