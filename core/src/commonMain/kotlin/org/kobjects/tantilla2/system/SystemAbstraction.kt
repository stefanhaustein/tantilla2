package org.kobjects.tantilla2.system

interface SystemAbstraction {
    fun write(s: String)

    fun launch(task: (ThreadHandle) -> Unit): ThreadHandle

    fun createLock(): Lock

    fun input(): String


}