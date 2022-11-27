package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.function.Parameter

class MutableListType(
    elementType: Type
) : ListType(
    elementType,
    "MutableList[${elementType.typeName}]"
) {


    override fun create(size: Int, init: (Int) -> Any?) = MutableTypedList(this, MutableList(size, init))


}