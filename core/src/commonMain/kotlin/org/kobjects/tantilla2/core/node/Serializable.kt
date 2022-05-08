package org.kobjects.tantilla2.core.node

interface Serializable {
    fun serialize(sb: StringBuilder, indent: String, prcedence: Int = 0)
}