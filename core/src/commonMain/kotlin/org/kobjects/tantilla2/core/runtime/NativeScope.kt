package org.kobjects.tantilla2.core.runtime

import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunction
import org.kobjects.tantilla2.core.function.Parameter

open class NativeScope(var name: String, parent: Scope) : Scope(parent) {
    override val title: String
        get() = name

}