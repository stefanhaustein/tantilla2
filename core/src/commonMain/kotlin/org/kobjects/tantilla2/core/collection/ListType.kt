package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.node.expression.FloatNode
import org.kobjects.tantilla2.core.type.FloatType
import org.kobjects.tantilla2.core.type.GenericType
import org.kobjects.tantilla2.core.type.IntType
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.stdlib.graphics.Color

open class ListType(
    val elementType: Type,
    name: String = "List",
    docString: String = "An immutable list of elements.",
    ctor:  (LocalRuntimeContext) -> Any? = { TypedList(elementType, it.get(0) as List<Any>) }
) : NativeStructDefinition(
    null,
    name,
    docString,
    listOf(Parameter("elements", elementType, isVararg = true)),
    ctor
    ), CollectionType, GenericType {

    init {
        defineMethod("len", "Returns the length of the list", IntType) {
            (it[0] as TypedList).size.toLong()
        }

        defineMethod("index", "Returns the index of the value in the list, or -1 if not found.",
            IntType, Parameter("value", elementType)) {
            (it[0] as TypedList).data.indexOf(it[1]).toLong()
        }

        defineNativeFunction(
            "init",
            "Create a list of the given size, filled using the function parameter",
            this,
            Parameter("len", IntType),
            Parameter("fill", FunctionType.Impl(elementType, listOf(Parameter("index", IntType)))),
        ) { context ->
            val size = context.i32(0)
            val fn = context[1] as Callable
            val functionContext = LocalRuntimeContext(
                context.globalRuntimeContext,
                fn.scopeSize,
                closure = fn.closure,
            )
            TypedList(elementType, List(size) {
                functionContext.variables[0] = it.toLong()
                fn.eval(functionContext)!!
            } )
        }

    }

    override fun serializeType(writer: CodeWriter, scope: Scope?) {
        writer.append(name).append("[").appendType(elementType, scope).append("]")
    }

    open fun create(size: Int, init: (Int) -> Any) = TypedList(this, MutableList(size, init))

    override val genericParameterTypes: List<Type> = listOf(elementType)

    override fun create(types: List<Type>) = ListType(types.first())

    override fun isAssignableFrom(other: Type) = other is ListType && other.elementType == elementType

    override fun equals(other: Any?): Boolean =
        other is ListType && other.elementType == elementType && other !is MutableListType
}