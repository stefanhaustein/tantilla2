package org.kobjects.tantilla2.core.scope

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.function.*
import org.kobjects.tantilla2.core.type.*
import org.kobjects.tantilla2.core.system.SystemAbstraction

class SystemRootScope(
    val systemAbstraction: SystemAbstraction,
    val runStateCallback: (GlobalRuntimeContext) -> Unit = {}
) : UnitScope(AbsoluteRootScope, "<SystemRootScope>", "System Root Scope") {

    init {
        defineNativeFunction("print",
                "Print the value of the text parameter to the console.",
                NoneType, Parameter("value", StringableType, isVararg = true)
            ) {
                val list = it[0] as Iterable<Any?>
                systemAbstraction.write(list.joinToString(" "))
            }

        defineNativeFunction("input", "Prompt the user for input.",
            StrType,
            Parameter("label", StrType))
            {
                systemAbstraction.input(it[0].toString())
            }


    }


    override val parentScope: Scope?
        get() = AbsoluteRootScope

    override val name: String
        get() = "System Root Scope"

    override val kind: Definition.Kind
        get() = Definition.Kind.UNIT

    override var docString: String
        get() = ""
        set(_) = throw UnsupportedOperationException()



}