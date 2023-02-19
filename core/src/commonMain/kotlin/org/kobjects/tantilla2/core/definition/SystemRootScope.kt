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
        add(BoolType)
        add(FloatType)
        add(IntType)
        add(StringableType)
        add(PairType(TypeVariable("A"), TypeVariable("B")))

        val listType = ListType(TypeVariable("E"))
        add(listType)
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

        val typeVariable = TypeVariable("T")

        defineNativeFunction(
            "while",
            "Execute the 2nd argument while the first argument is true.",
            NoneType,
            Parameter("condition", FunctionType.Impl(BoolType, emptyList())),
            Parameter("body", FunctionType.Impl(NoneType, emptyList())),
        ) {
            val condition = it[0] as Callable
            val body = it[1] as Callable
            var ctrl : Any = NoneType.None
            while (true) {
                val conditionContext = LocalRuntimeContext(it.globalRuntimeContext, condition)
                val conditionResult = condition.eval(conditionContext) as Boolean
                if (!conditionResult) {
                    break
                }
                val bodyContext = LocalRuntimeContext(it.globalRuntimeContext, body)
                try {
                    body.eval(bodyContext)
                } catch (signal: LoopControlSignal) {
                    when(signal.kind) {
                        LoopControlSignal.Kind.BREAK -> break
                        LoopControlSignal.Kind.CONTINUE -> continue
                    }
                }
            }
            NoneType.None
        }

        defineNativeFunction("if",
            "Conditional",
            typeVariable,
            Parameter("condition", BoolType),
            Parameter("then", FunctionType.Impl(typeVariable, emptyList())),
            Parameter(
                "elif",
                PairType(FunctionType.Impl(BoolType, emptyList()), FunctionType.Impl(typeVariable, emptyList())),
                isVararg = true),
            Parameter("else", FunctionType.Impl(typeVariable, emptyList()), object : LeafNode() {
                override fun eval(context: LocalRuntimeContext) = CallableImpl(FunctionType.Impl(NoneType, emptyList()), 0, body = object : Evaluable {
                    override fun eval(context: LocalRuntimeContext): Any {
                        return NoneType.None
                    }

           /*         override val returnType: Type
                        get() = NoneType */
                })
                override val returnType: Type
                    get() = FunctionType.Impl(NoneType, emptyList())

                override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
                    writer.appendCode("None")
                }
            }),
        ) {
            if (it[0] as Boolean) {
                val then = it[1] as Callable
                val ctx = LocalRuntimeContext(it.globalRuntimeContext, then)
                then.eval(ctx)
            } else {
                var found: Any? = null
                for (rawElif in it[2] as List<*>) {
                    val elif = rawElif as Pair<Any, Any>
                    val condition = elif.first as Callable
                    val conditionContext = LocalRuntimeContext(it.globalRuntimeContext, condition)
                    val result = condition.eval(conditionContext)
                    if (result as Boolean) {
                        val then = elif.second as Callable
                        val thenContent = LocalRuntimeContext(it.globalRuntimeContext, then)
                        found = then.eval(thenContent)
                        break
                    }
                }
                if (found != null) {
                    found
                } else {
                    val elze = it[3] as Callable
                    val ctx = LocalRuntimeContext(it.globalRuntimeContext, elze)
                    elze.eval(ctx)
                }
            }
        }

        add(iteratorTrait)
        add(iterableTrait)

        add(IterableImpl(this, listType, ""))
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


    companion object {
        val iteratorTrait = IteratorTrait()
        val iterableTrait = IterableTrait()
    }

}