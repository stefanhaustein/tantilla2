package org.kobjects.tantilla2.core

interface SystemAbstraction {
    fun write(s: String)

    fun launch(task: () -> Unit)

    fun createLock(): Lock

    fun input(): String
}