package org.kobjects.tantilla2.testing

import org.kobjects.tantilla2.core.Lock
import org.kobjects.tantilla2.core.SystemAbstraction
import org.kobjects.tantilla2.core.definition.UserRootScope
import org.kobjects.tantilla2.core.definition.SystemRootScope

object TestSystemAbstraction : SystemAbstraction {
    override fun write(s: String) {
        println(s)
    }

    override fun launch(task: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun createLock(): Lock {
        TODO("Not yet implemented")
    }

    override fun input(): String {
        TODO("Not yet implemented")
    }

    fun createScope() = UserRootScope(SystemRootScope(TestSystemAbstraction))
}