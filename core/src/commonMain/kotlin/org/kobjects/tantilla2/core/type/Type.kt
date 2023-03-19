package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.classifier.TraitDefinition.Companion.isConvertibleFrom
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.UserRootScope


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

    fun withGenericsResolved(typeList: List<Type>): Type =
        throw UnsupportedOperationException()

    val unparameterized: Type?
        get() = this

    fun unparameterized(): Type = unparameterized ?: this

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

    fun equalsIgnoringTypeVariables(other: Type) =
        this is TypeParameter || other is TypeParameter || this == other || this.genericParameterTypes.isNotEmpty() || other.genericParameterTypes.isNotEmpty()

    fun containsUnresolvedTypeParameters(map: GenericTypeMap) = genericParameterTypes.any { it is TypeParameter && map[it] == null }


    fun mapTypeParametersToTypeVariables(map: GenericTypeMap): Type {
        if (!containsUnresolvedTypeParameters(map)) {
            return this
        }
        val replacements = genericParameterTypes.map {
            if (it is TypeParameter) map.createVariable() else it
        }
        return withGenericsResolved(replacements)
    }

    fun mapTypes(mapping: (Type) -> Type) = mapping(this)

    /** Resolve generics in this type. Actual is the type implied by code, if available */
    fun resolveGenerics(
        actualType: Type?,
        map: GenericTypeMap,
        allowNoneMatch: Boolean = false, //
        allowAs: UserRootScope? = null,
    ): Type {
        if (containsUnresolvedTypeParameters(map)) {
            val parametersResolved = mapTypeParametersToTypeVariables(map)
            return parametersResolved.resolveGenerics(actualType, map, allowNoneMatch, allowAs)
        }

        if (actualType == null) {
            return mapTypes(map::map)
        }

        return resolveGenericsImpl(actualType, map, allowNoneMatch, allowAs)
    }

    fun resolveGenericsImpl(
        actualType: Type,
        map: GenericTypeMap,
        allowNoneMatch: Boolean = false, //
        allowAs: UserRootScope? = null,
    ): Type {
        if (actualType != this) {
            if (allowAs != null) {
                if (isConvertibleFrom(actualType, allowAs)) {
                    return this
                }
                throw IllegalArgumentException("Type mismatch. Expected: $this (unparameterized:{${this.unparameterized()}); actual: $actualType  ${allowAs!!.traitToClass[this.unparameterized ?: this]}")
             }
            throw IllegalArgumentException("Type mismatch. Expected: $this actual: $actualType")
        }
        return this
    }

}