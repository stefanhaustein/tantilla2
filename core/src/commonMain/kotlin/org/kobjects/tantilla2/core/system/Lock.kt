package org.kobjects.tantilla2.core.system

interface Lock {
    fun guard(task: () -> Unit)
}