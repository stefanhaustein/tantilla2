package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Control
import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.F64

fun Any?.serialize() =
    when (this) {
        is Evaluable<*> -> (this as Evaluable<RuntimeContext>).serialize("")
        else -> this.toString()
    }



    fun serializeInfix(expr: Evaluable<RuntimeContext>, name: String, precedence: Int, parentPrecedence: Int): String {
        val sb = StringBuilder()
        sb.append(expr.children()[0].serialize("", precedence))
        sb.append(" $name ")
        sb.append(expr.children()[1].serialize( "", precedence))
        return sb.toString()
    }

    fun serializeIf(expr: Control.If<RuntimeContext>, indent: String): String {
        val sb = StringBuilder()
        sb.append("if ")
        sb.append(expr.ifThenElse[0].serialize())
        sb.append(":\n$indent")
        sb.append(expr.ifThenElse[1].serialize(), "  $indent")

        for (i in 2 until expr.ifThenElse.size step 2) {
            sb.append("elif ")
            sb.append(expr.ifThenElse[i].serialize())
            sb.append(":\n$indent")
            sb.append(expr.ifThenElse[i + 1].serialize("  $indent"))
        }
        if (expr.ifThenElse.size % 2 == 1) {
            sb.append("\n${indent}else:\n$indent")
            sb.append(expr.ifThenElse[expr.ifThenElse.size - 1].serialize("  $indent"))
        }
        return sb.toString()
    }


    fun serializeWhile(expr: Control.While<RuntimeContext>, indent: String): String {
        val sb = StringBuilder()
        sb.append("while ")
        sb.append(expr.condition.serialize(indent))
        sb.append(":\n$indent")
        sb.append(expr.body.serialize( "  $indent"))
        return sb.toString()
    }

    fun Evaluable<RuntimeContext>.serialize(indent: String = "", parentPrecedence: Int = 0): String =
        when (this) {
            is F64.Add -> serializeInfix(this, "+", 1, parentPrecedence)
            is F64.Sub -> serializeInfix(this, "-", 1, parentPrecedence)
            is F64.Mul -> serializeInfix(this, "*", 2, parentPrecedence)
            is F64.Div -> serializeInfix(this, "/", 2, parentPrecedence)
            is Control.If -> serializeIf(this, indent)
            is Control.Block -> statements.joinToString("\n$indent") { it.serialize(indent) }
            is Control.While -> serializeWhile(this, indent)
            else -> toString()
        }


