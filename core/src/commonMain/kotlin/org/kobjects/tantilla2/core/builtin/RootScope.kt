package org.kobjects.tantilla2.core.builtin

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.Parameter

class RootScope(
    val systemAbstraction: SystemAbstraction,
    val runStateCallback: (GlobalRuntimeContext) -> Unit = {}
) : Scope() {

    init {
        add(BoolType)
        add(FloatType)
        add(IntType)
        add(ListType(AnyType))
        add(StrType)

        add(MathScope)

        defineNativeFunction("print",
                "Print the value of the text parameter to the console.",
                VoidType, Parameter("value", AnyType, isVararg = true)
            ) {
                val list = it[0] as List<Any?>
                systemAbstraction.write(list.joinToString(" "))
            }


    }


    override val parentScope: Scope?
        get() = null

    override val name: String
        get() = "Root Scope"

    override val kind: Definition.Kind
        get() = Definition.Kind.UNIT

    override var docString: String
        get() = ""
        set(_) = throw UnsupportedOperationException()

}