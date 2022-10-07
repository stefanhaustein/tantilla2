package org.kobjects.tantilla2

import org.kobjects.tantilla2.core.Lock
import org.kobjects.tantilla2.core.SystemAbstraction

object TestSystem : SystemAbstraction {
    override fun write(s: String) {
        println(s)
    }

    override fun launch(task: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun createLock(): Lock {
        TODO("Not yet implemented")
    }
}