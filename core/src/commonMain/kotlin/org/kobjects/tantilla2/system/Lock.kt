package org.kobjects.tantilla2.system

interface Lock {
    fun guard(task: () -> Unit)
}