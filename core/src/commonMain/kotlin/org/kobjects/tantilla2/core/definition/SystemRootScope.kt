package org.kobjects.tantilla2.core.definition

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.collection.*
import org.kobjects.tantilla2.core.node.LeafNode
import org.kobjects.tantilla2.core.node.expression.StrNode
import org.kobjects.tantilla2.core.control.LoopControlSignal
import org.kobjects.tantilla2.core.function.*
import org.kobjects.tantilla2.core.type.*
import org.kobjects.tantilla2.stdlib.math.MathScope
import org.kobjects.tantilla2.core.system.SystemAbstraction

class SystemRootScope(
    val systemAbstraction: SystemAbstraction,
    val runStateCallback: (GlobalRuntimeContext) -> Unit = {}
) : Scope() {

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