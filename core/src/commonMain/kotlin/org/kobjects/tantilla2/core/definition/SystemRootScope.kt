package org.kobjects.tantilla2.core.definition

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.collection.*
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.node.expression.StrNode
import org.kobjects.tantilla2.core.type.*
import org.kobjects.tantilla2.stdlib.math.MathScope
import org.kobjects.tantilla2.system.SystemAbstraction

class SystemRootScope(
    val systemAbstraction: SystemAbstraction,
    val runStateCallback: (GlobalRuntimeContext) -> Unit = {}
) : Scope() {

    init {
        add(BoolType)
        add(FloatType)
        add(IntType)
        add(StringableType)
        add(PairType(TypeVariable("A"), TypeVariable("B")))
        add(ListType(TypeVariable("E")))
        add(MutableListType(TypeVariable("E")))
        add(MapType(TypeVariable("K"), TypeVariable("V")))
        add(MutableMapType(TypeVariable("K"), TypeVariable("V")))
        add(SetType(TypeVariable("E")))
        add(MutableSetType(TypeVariable("E")))
        add(OptionalType(TypeVariable("V")))
        add(StrType)

        add(MathScope)

        defineNativeFunction("print",
                "Print the value of the text parameter to the console.",
                NoneType, Parameter("value", StringableType, isVararg = true)
            ) {
                val list = it[0] as Iterable<Any?>
                systemAbstraction.write(list.joinToString(" "))
            }

        defineNativeFunction("input", "Prompt the user for input.",
            StrType)
            {
                systemAbstraction.input()
            }

        defineNativeFunction("assert", "Throws an exception if the argument does not evaluate to true.",
            NoneType, Parameter("condition", BoolType), Parameter("message", StrType, StrNode.Const("Assertion failed"))) {
            if (!(it[0] as Boolean)) {
                throw IllegalArgumentException(it[1] as String)
            }
        }

        defineNativeFunction("run", "Resets the program state and runs the main() function.",
            NoneType) {
            it.globalRuntimeContext.run(calledFromCode = true)
        }

        defineNativeFunction("str",
            "Converts the value to a string.",
            StrType, Parameter("value", StringableType)
        ) {
            it.get(0).toString()
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