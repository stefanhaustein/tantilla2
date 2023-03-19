package org.kobjects.tantilla2.testing

import org.kobjects.tantilla2.core.system.Lock
import org.kobjects.tantilla2.core.system.SystemAbstraction
import org.kobjects.tantilla2.core.scope.UserRootScope
import org.kobjects.tantilla2.core.scope.SystemRootScope
import org.kobjects.tantilla2.core.system.ThreadHandle

object TestSystemAbstraction : SystemAbstraction {
    override fun write(s: String) {
        println(s)
    }

    override fun launch(task: (ThreadHandle) -> Unit): ThreadHandle {
        TODO("Not yet implemented")
    }

    override fun createLock(): Lock {
        TODO("Not yet implemented")
    }

    override fun input(label: String?): String {
        TODO("Not yet implemented")
    }

    fun createScope() = UserRootScope(SystemRootScope(TestSystemAbstraction))
}