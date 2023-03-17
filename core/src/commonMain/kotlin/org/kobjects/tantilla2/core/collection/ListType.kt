package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.classifier.AdapterInstance
import org.kobjects.tantilla2.core.classifier.NativeAdapter
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.definition.AbsoluteRootScope
import org.kobjects.tantilla2.core.definition.SystemRootScope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.IntType
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.TypeParameter

open class ListType(
    name: String = "List",
    docString: String = "An immutable list of elements.",
    ctor:  (LocalRuntimeContext) -> Any = { it.get(0) }
) : NativeStructDefinition(
    null,
    name,
    docString,
    ctor,
    Parameter("elements", ELEMENT_TYPE, isVararg = true),
), CollectionType {

    init {
        defineMethod("len", "Returns the length of the list", IntType) {
            (it[0] as List<*>).size.toLong()
        }

        defineMethod("index", "Returns the index of the value in the list, or -1 if not found.",
            IntType, Parameter("value", ELEMENT_TYPE)) {
            (it[0] as List<*>).indexOf(it[1]).toLong()
        }

        defineMethod(
            "iterator",
            "Returns an iterator for this list",
            AbsoluteRootScope.iteratorTrait.withElementType(ELEMENT_TYPE)
        ) {
            IteratorTrait.createAdapter((it[0] as List<Any>).iterator())
        }

        defineNativeFunction(
            "init",
            "Create a list of the given size, filled using the function parameter",
            this,
            Parameter("len", IntType),
            Parameter("fill", FunctionType.Impl(ELEMENT_TYPE, listOf(Parameter("index", IntType)))),
        ) { context ->
            val size = context.i32(0)
            val fn = context[1] as Callable
            val functionContext = LocalRuntimeContext(
                context.globalRuntimeContext,
                fn
            )
            List(size) {
                functionContext.variables[0] = it.toLong()
                fn.eval(functionContext)!!
            }
        }
    }

    open fun create(size: Int, init: (Int) -> Any) = MutableList(size, init)

    override val genericParameterTypes = listOf(ELEMENT_TYPE)

//        ListType(genericTypeList[0], unparameterized = unparameterized ?: this)
/*
    override fun isAssignableFrom(other: Type) = other is ListType && other.elementType == elementType

    override fun equals(other: Any?): Boolean =
        other is ListType && other.elementType == elementType && other !is MutableListType

*/


    companion object {
        val ELEMENT_TYPE = TypeParameter("E")
    }

    }
