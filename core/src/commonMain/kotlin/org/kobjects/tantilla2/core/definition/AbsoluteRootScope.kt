package org.kobjects.tantilla2.core.definition

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Evaluable
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.classifier.Adapter
import org.kobjects.tantilla2.core.collection.*
import org.kobjects.tantilla2.core.control.LoopControlSignal
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.CallableImpl
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.node.LeafNode
import org.kobjects.tantilla2.core.node.expression.StrNode
import org.kobjects.tantilla2.core.type.*
import org.kobjects.tantilla2.stdlib.math.MathScope

object AbsoluteRootScope : Scope() {
    override val parentScope: Scope?
        get() = null
    override val kind: Definition.Kind
        get() = Definition.Kind.UNIT
    override val name: String
        get() = "Absolute Root Scope"

    val iteratorTrait = IteratorTrait()
    val iterableTrait = IterableTrait()


    init {
        add(BoolType)
        add(FloatType)
        add(IntType)
        add(StringableType)
        add(PairType)
        val listType = ListType()
        add(listType)
        add(MutableListType)
        add(MapType())
        add(MutableMapType)
        add(SetType())
        add(MutableSetType)
        add(OptionalType(TypeParameter("V")))
        add(StrType)
        add(MathScope)

        add(iteratorTrait)
        add(iterableTrait)

        add(IterableImpl(this, listType, ""))
        add(IterableImpl(this, MutableListType, ""))

        add(RangeType)
        add(IterableImpl(this, RangeType, ""))



        defineNativeFunction("str",
            "Converts the value to a string.",
            StrType, Parameter("value", StringableType)
        ) {
            it.get(0).toString()
        }

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


        val ifTypeParameter = TypeParameter("T")
        defineNativeGenericFunction(
            listOf(ifTypeParameter),
            "if",
            "Conditional",
            ifTypeParameter,
            Parameter("condition", BoolType),
            Parameter("then", FunctionType.Impl(ifTypeParameter, emptyList())),
            Parameter(
                "elif",
                PairType.withGenericsResolved(listOf(FunctionType.Impl(BoolType, emptyList()), FunctionType.Impl(ifTypeParameter, emptyList()))),
                isVararg = true),
            Parameter("else", FunctionType.Impl(ifTypeParameter, emptyList()), object : LeafNode() {
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

        defineNativeGenericFunction(
            listOf(iterableTrait.elementType),
            "for2",
            "Iterate over the loop expression in the body.",
            NoneType,
            Parameter("iterable", iterableTrait),
            Parameter("body", FunctionType.Impl(NoneType, listOf(Parameter("i", iterableTrait.elementType)))),
        ) {
            val iterable = it[0] as Adapter
            val body = it[1] as Callable
            val iterator = iterable.evalMethod(0, it) as Adapter

            while (iterator.evalMethod(0, it) as Boolean) {
                val item = iterator.evalMethod(1, it)
                val bodyContext = LocalRuntimeContext(it.globalRuntimeContext, body) { item }
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

    }




}