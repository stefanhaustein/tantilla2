package org.kobjects.tantilla2.core.definition

import org.kobjects.tantilla2.core.LocalRuntimeContext
import toLiteral

/**
 * Something that "owns" a local runtime context; Used to render instances.
 *
 * Not a subclass of Scope because Callable implements this
 */
interface DynamicScope {
    val dynamicScopeSize: Int
    val closure: LocalRuntimeContext?
        get() = null

    fun toString(context: LocalRuntimeContext): String {
        if (this is Scope && this !is UserRootScope) {
            val sb = StringBuilder(name)
            sb.append("(")
            for (i in locals.indices) {
                if (i > 0) {
                    sb.append(", ")
                }
                sb.append(locals[i])
                sb.append(" = ")
                sb.append(context.variables[i].toLiteral())
            }
            return sb.toString()
        }
        return  "DynamicScope(${context.variables})"
    }
}