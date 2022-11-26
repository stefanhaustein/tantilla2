package org.kobjects.tantilla2.core.definition

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.node.expression.StrNode
import org.kobjects.tantilla2.core.type.*
import org.kobjects.tantilla2.stdlib.math.MathScope

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

        defineNativeFunction("assert", "Throws an exception if the argument does not evaluate to true.",
            VoidType, Parameter("condition", BoolType), Parameter("message", StrType, StrNode.Const("Assertion failed"))) {
            if (!(it[0] as Boolean)) {
                throw IllegalArgumentException(it[1] as String)
            }
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