package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.collection.ListType
import org.kobjects.tantilla2.core.collection.TypedList
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.node.expression.IntNode
import org.kobjects.tantilla2.core.node.expression.StrNode

object StrType : NativeStructDefinition(
    null,
    "str",
    "A character String. Use the constructor to convert a value to its string representation.",
    ctorParams = listOf(Parameter("value", AnyType, defaultValueExpression = StrNode.Const(""))),
    ctor = {
        it[0].toString()
    }
)  {
    init {
        defineMethod("int",
            "Converts this string to an integer value.",
            IntType,
            Parameter("radix", IntType, IntNode.Const(10))
        ) {
            (it[0] as String).toInt((it[1] as Long).toInt())
        }
        defineMethod("float",
            "Converts this string to a float value.",
            IntType) {
            (it[0] as String).toFloat()
        }

        defineMethod(
            "len",
            "Returns the length of the string",
            IntType
        ) {
            (it[0] as String).length.toLong()
        }
        defineMethod(
            "join",
            "Joins the list parameter, separating elements with this string.",
            StrType,
            Parameter("list", ListType(StrType))
        ) {
            (it[1] as TypedList).data.joinToString(it[0] as String)
        }
        defineMethod(
            "ord",
            "Returns the value of the first code point in this string",
            IntType
        ) {
            (it[0] as String).first().code.toLong()
        }
        defineMethod(
            "split",
            "Split the string at given delimiter",
            ListType(StrType),
            Parameter("delimiter", StrType)
        ) {
            TypedList(StrType, (it[0] as String).split(it[1] as String))
        }
    }
}