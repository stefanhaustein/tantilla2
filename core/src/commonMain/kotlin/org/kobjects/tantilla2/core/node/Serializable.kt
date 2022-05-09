package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.CodeWriter

interface Serializable {
    fun serialize(writer: CodeWriter, prcedence: Int = 0)
}