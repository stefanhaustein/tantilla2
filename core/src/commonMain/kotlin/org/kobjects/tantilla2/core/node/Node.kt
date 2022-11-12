package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.SerializableCode

/**
 * Abstract serializable evaluable with toString implemented based on serialization.
 */

abstract class Node : Evaluable, SerializableCode {

    final override fun toString() = CodeWriter().appendCode(this).toString()
}