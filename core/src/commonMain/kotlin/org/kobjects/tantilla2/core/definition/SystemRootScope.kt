package org.kobjects.tantilla2.core.definition

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.collection.*
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.CallableImpl
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.node.LeafNode
import org.kobjects.tantilla2.core.node.expression.StrNode
import org.kobjects.tantilla2.core.type.*
import org.kobjects.tantilla2.stdlib.math.MathScope
import org.kobjects.tantilla2.core.system.SystemAbstraction

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
            StrType,
            Parameter("label", StrType))
            {
                systemAbstraction.input(it[0].toString())
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

        defineNativeFunction("iif",
            "Conditional",
            NoneType,
            Parameter("condition", BoolType),
            Parameter("then", FunctionType.Impl(NoneType, emptyList())),
            Parameter("elseif", PairType(FunctionType.Impl(BoolType, emptyList()), FunctionType.Impl(NoneType, emptyList())), isVararg = true),
            Parameter("else", FunctionType.Impl(NoneType, emptyList()), object : LeafNode() {
                override fun eval(context: LocalRuntimeContext) = CallableImpl(FunctionType.Impl(NoneType, emptyList()), 0, body = object : Evaluable {
                    override fun eval(context: LocalRuntimeContext): Any {
                        return NoneType.None
                    }

                    override val returnType: Type
                        get() = NoneType
                })
                override val returnType: Type
                    get() = FunctionType.Impl(NoneType, emptyList())

                override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
                    writer.appendCode("# Default parameter")
                }
            }),
        ) {
            if (it[0] as Boolean) {
                val then = it[1] as Callable
                val ctx = LocalRuntimeContext(it.globalRuntimeContext, then)
                then.eval(ctx)
            } else {
                val elze = it[3] as Callable
                val ctx = LocalRuntimeContext(it.globalRuntimeContext, elze)
                elze.eval(ctx)
            }

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