package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.classifier.NativeTypeDefinition
import org.kobjects.tantilla2.core.type.BoolType
import org.kobjects.tantilla2.core.type.Type

class NativeIteratorType(elementType: Type) : NativeTypeDefinition(null, "NativeIterator", "") {

    init {
        defineMethod("has_next", "", BoolType) {
            (it[0] as Iterator<Any>).hasNext()
        }
        defineMethod("next", "", elementType) {
            (it[0] as Iterator<Any>).next()
        }
    }

}