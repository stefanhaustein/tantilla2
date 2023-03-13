package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.definition.DocStringUpdatable
import org.kobjects.tantilla2.core.definition.UserRootScope
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.MetaType
import org.kobjects.tantilla2.core.type.Type

/**
 * A typed scope that has some form of instances.
 */
abstract class Classifier : Scope(), Type, DocStringUpdatable {

    override fun serializeType(writer: CodeWriter) {
        serializeQualifiedName(writer)
    }

    override val supportsMethods: Boolean
        get() = true

    override fun resolve(name: String) = resolveDynamic(name, false)

    override val type: Type
        get() = MetaType(this)


    override fun equals(other: Any?): Boolean =
        other is Classifier
                && other.parentScope == parentScope
                && other.name == name
                && other.genericParameterTypes == genericParameterTypes

    override fun mapTypes(mapping: (Type) -> Type): Type {
        if (mapping(this) != this) {
            return mapping(this)
        }
                
        var anyChanged = false
        val resolvedParameters = List(genericParameterTypes.size) {
            val oldType = genericParameterTypes[it]
            val newType = oldType.mapTypes(mapping)
            if (newType != genericParameterTypes[it]) {
                anyChanged = true
            }
            newType
        }
        return if (anyChanged) withGenericsResolved(resolvedParameters) else this
    }

    override fun withGenericsResolved(genericTypeList: List<Type>): Classifier =
        ResolvedGenericType((this.unparameterized ?: this) as Classifier, genericTypeList)


    override fun resolveGenericsImpl(
        actualType: Type,
        map: GenericTypeMap,
        allowNoneMatch: Boolean,
        allowAs: UserRootScope?
    ): Type {
        if (actualType !is Classifier
            || actualType.parentScope != parentScope
            || actualType.name != name
            || genericParameterTypes.isEmpty()
        ) {
            return super.resolveGenericsImpl(actualType, map, allowNoneMatch, allowAs)
        }

        val resolvedParameters = List<Type>(genericParameterTypes.size) {
            genericParameterTypes[it].resolveGenerics(actualType.genericParameterTypes[it], map)
        }

        return withGenericsResolved(resolvedParameters)
    }

}