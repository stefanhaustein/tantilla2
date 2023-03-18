package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.Type

object MutableMapType : MapType(
    "MutableMap",
    "A mutable map.",
     { mutableMapOf<Any, Any>() },
) {

}