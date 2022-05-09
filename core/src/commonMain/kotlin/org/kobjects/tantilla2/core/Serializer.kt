package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Control
import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.F64
import org.kobjects.tantilla2.core.node.Serializable

fun Any?.serialize(indent: String = ""): String {
    val sb = CodeWriter(indent)
    this.serialize(sb)
    return sb.toString()
}

fun Any?.serialize(sb: CodeWriter, parentPrecedence: Int = 0) {
    when (this) {
        is Serializable -> serialize(sb, parentPrecedence)
        is Evaluable<*> -> serializeEvaluable(sb, parentPrecedence, this as Evaluable<RuntimeContext>)
        else -> sb.append(toString())
    }
}

fun serializeInfix(
    sb: CodeWriter,
    parentPrecedence: Int,
    expr: Evaluable<RuntimeContext>,
    name: String,
    precedence: Int,
) {
    expr.children()[0].serialize(sb, precedence)
    sb.append(" $name ")
    expr.children()[1].serialize(sb, precedence)
}

fun serializeIf(
    sb: CodeWriter,
    parentPrecedence: Int,
    expr: Control.If<RuntimeContext>,
) {
    sb.append("if ")
    expr.ifThenElse[0].serialize(sb)
    sb.append(':').indent().newline()
    expr.ifThenElse[1].serialize(sb)
    sb.outdent()

    for (i in 2 until expr.ifThenElse.size - 1 step 2) {
        sb.newline().append("elif ").indent()
        expr.ifThenElse[i].serialize(sb)
        sb.append(':').newline()
        expr.ifThenElse[i + 1].serialize(sb)
        sb.outdent()
    }
    if (expr.ifThenElse.size % 2 == 1) {
        sb.newline().append("else:").indent().newline()
        expr.ifThenElse[expr.ifThenElse.size - 1].serialize(sb)
        sb.outdent()
    }
}

fun serializeWhile(
    sb: CodeWriter,
    parentPrecedence: Int,
    expr: Control.While<RuntimeContext>,
) {
    sb.append("while ")
    expr.condition.serialize(sb)
    sb.append(':').indent().newline()
    expr.body.serialize(sb)
    sb.outdent()
}

fun serializeBlock(sb: CodeWriter, parentPrecedence: Int, block: Control.Block<RuntimeContext>) {
    if (block.statements.size > 0) {
        block.statements[0].serialize(sb)
        for (i in 1 until block.statements.size) {
            sb.newline()
            block.statements[i].serialize(sb)
        }
    }
}

fun serializeEvaluable(sb: CodeWriter, parentPrecedence: Int = 0, expr: Evaluable<RuntimeContext>) {
    when (expr) {
        is F64.Add -> serializeInfix(sb, parentPrecedence, expr, "+", 1)
        is F64.Sub -> serializeInfix(sb, parentPrecedence, expr, "-", 1)
        is F64.Mul -> serializeInfix(sb, parentPrecedence, expr, "*", 2)
        is F64.Div -> serializeInfix(sb, parentPrecedence, expr, "/", 2)
        is Control.If -> serializeIf(sb, parentPrecedence, expr)
        is Control.Block -> serializeBlock(sb, parentPrecedence, expr)
        is Control.While -> serializeWhile(sb, parentPrecedence, expr)
        else -> sb.append(expr.toString())
    }
}
