package org.kobjects.tantilla2.core

interface Lock {
    fun guard(task: () -> Unit)
}