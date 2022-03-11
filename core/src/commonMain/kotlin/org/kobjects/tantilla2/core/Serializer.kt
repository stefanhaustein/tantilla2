package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Control
import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.F64

object Serializer {

    fun serializeInfix(expr: Evaluable<RuntimeContext>, name: String, precedence: Int, parentPrecedence: Int): String {
        val sb = StringBuilder()
        sb.append(serialize(expr.children()[0], "", precedence))
        sb.append(" $name ")
        sb.append(serialize(expr.children()[1], "", precedence))
        return sb.toString()
    }

    fun serializeIf(expr: Control.If<RuntimeContext>, indent: String): String {
        val sb = StringBuilder()
        sb.append("if ")
        sb.append(serialize(expr.condition))
        sb.append(":\n$indent")
        sb.append(serialize(expr.then, "  $indent"))
        sb.append("\n${indent}else:\n$indent")
        sb.append(serialize(expr.otherwise, "  $indent"))
        return sb.toString()
    }


    fun serializeWhile(expr: Control.While<RuntimeContext>, indent: String): String {
        val sb = StringBuilder()
        sb.append("while ")
        sb.append(serialize(expr.condition))
        sb.append(":\n$indent")
        sb.append(serialize(expr.body, "  $indent"))
        return sb.toString()
    }

    fun serialize(expr: Evaluable<RuntimeContext>, indent: String = "", parentPrecedence: Int = 0): String =
        when (expr) {
            is F64.Add -> serializeInfix(expr, "+", 1, parentPrecedence)
            is F64.Sub -> serializeInfix(expr, "-", 1, parentPrecedence)
            is F64.Mul -> serializeInfix(expr, "*", 2, parentPrecedence)
            is F64.Div -> serializeInfix(expr, "/", 2, parentPrecedence)
            is Control.If -> serializeIf(expr, indent)
            is Control.Block -> expr.statements.joinToString("\n$indent") { serialize(it, indent) }
            is Control.While -> serializeWhile(expr, indent)
            else -> expr.toString()
        }





}