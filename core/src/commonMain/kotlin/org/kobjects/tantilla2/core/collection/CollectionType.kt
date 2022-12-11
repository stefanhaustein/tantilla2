package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.type.GenericType
import org.kobjects.tantilla2.core.type.Type

interface CollectionType : Type {
    val genericParameterTypes: List<Type>
}