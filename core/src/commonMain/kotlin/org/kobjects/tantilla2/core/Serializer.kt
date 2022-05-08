package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Control
import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.F64
import org.kobjects.tantilla2.core.node.Serializable

fun Any?.serialize(indent: String = ""): String {
    val sb = StringBuilder()
    this.serialize(sb, indent)
    return sb.toString()
}

fun Any?.serialize(sb: StringBuilder, indent: String, parentPrecedence: Int = 0) {
    when (this) {
        is Serializable -> this.serialize(sb, indent, parentPrecedence)
        is Evaluable<*> -> serializeEvaluable(sb, indent, parentPrecedence, this as Evaluable<RuntimeContext>)
        else -> this.toString()
    }

}



fun serializeInfix(
    sb: StringBuilder,
    indent: String,
    parentPrecedence: Int,
    expr: Evaluable<RuntimeContext>,
    name: String,
    precedence: Int,
) {
    expr.children()[0].serialize(sb, indent, precedence)
    sb.append(" $name ")
    expr.children()[1].serialize(sb, indent, precedence)
}

fun serializeIf(
    sb: StringBuilder,
    indent: String,
    parentPrecedence: Int,
    expr: Control.If<RuntimeContext>,
) {
    val innerIndent = "  $indent"
    sb.append("if ")
    expr.ifThenElse[0].serialize(sb, indent)
    sb.append(":\n$innerIndent")
    expr.ifThenElse[1].serialize(sb, innerIndent)

    for (i in 2 until expr.ifThenElse.size - 1 step 2) {
        sb.append("\n${indent}elif ")
        expr.ifThenElse[i].serialize(sb, innerIndent)
        sb.append(":\n$innerIndent")
        expr.ifThenElse[i + 1].serialize(sb, innerIndent)
    }
    if (expr.ifThenElse.size % 2 == 1) {
        sb.append("\n${indent}else:\n$innerIndent")
        expr.ifThenElse[expr.ifThenElse.size - 1].serialize(sb, innerIndent)
    }
}

fun serializeWhile(
    sb: StringBuilder,
    indent: String,
    parentPrecedence: Int,
    expr: Control.While<RuntimeContext>,
) {
    sb.append("while ")
    expr.condition.serialize(sb, indent)
    sb.append(":\n$indent")
    expr.body.serialize(sb,  "  $indent")
}

fun serializeBlock(sb: StringBuilder, indent: String, parentPrecedence: Int, block: Control.Block<RuntimeContext>) {
    if (block.statements.size > 0) {
        block.statements[0].serialize(sb, indent)
        for (i in 1 until block.statements.size) {
            sb.append('\n').append(indent)
        }
    }
}

fun serializeEvaluable(sb: StringBuilder, indent: String = "", parentPrecedence: Int = 0, expr: Evaluable<RuntimeContext>) {
    when (expr) {
        is F64.Add -> serializeInfix(sb, indent, parentPrecedence, expr, "+", 1)
        is F64.Sub -> serializeInfix(sb, indent, parentPrecedence, expr, "-", 1)
        is F64.Mul -> serializeInfix(sb, indent, parentPrecedence, expr, "*", 2)
        is F64.Div -> serializeInfix(sb, indent, parentPrecedence, expr, "/", 2)
        is Control.If -> serializeIf(sb, indent, parentPrecedence, expr)
        is Control.Block -> serializeBlock(sb, indent, parentPrecedence, expr)
        is Control.While -> serializeWhile(sb, indent, parentPrecedence, expr)
        else -> sb.append(expr.toString())
    }
}

