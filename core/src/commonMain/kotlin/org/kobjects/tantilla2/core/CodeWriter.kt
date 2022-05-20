package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.*
import org.kobjects.konsole.Ansi

class CodeWriter(indent: String = "") : Appendable {
    val sb = StringBuilder()
    val indent = StringBuilder(indent)

    override fun append(value: Char): CodeWriter {
        sb.append(value)
        return this
    }

    override fun append(value: CharSequence?): CodeWriter {
        sb.append(value)
        return this
    }

    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Appendable {
        sb.append(value, startIndex, endIndex)
        return this
    }

    override fun toString() = sb.toString()

    fun indent(): CodeWriter {
        indent.append("  ")
        return this
    }

    fun outdent(): CodeWriter {
        indent.setLength(indent.length - 2)
        return this
    }

    fun keyword(name: String): CodeWriter {
        sb.append(Ansi.BOLD)
        sb.append(name)
        sb.append(Ansi.NORMAL_INTENSITY)
        return this
    }

    fun declaration(name: String): CodeWriter {
        sb.append(Ansi.BOLD).append(Ansi.FOREGROUND_CYAN)
        sb.append(name)
        sb.append(Ansi.NORMAL_INTENSITY).append(Ansi.FOREGROUND_DEFAULT)
        return this
    }

    fun newline(): CodeWriter {
        sb.append('\n').append(indent)
        return this
    }

    fun appendType(type: Type): CodeWriter {
        when (type) {
            is SerializableType -> type.serializeType(this)
            Type.F64 -> append("float")
            Type.Str -> append("str")
            else -> append(type.toString())
        }
        return this
    }

    fun appendCode(code: Any?, parentPrecedence: Int = 0): CodeWriter {
        when (code) {
            is SerializableCode -> code.serializeCode(this, parentPrecedence)
            is F64.Add<*> -> appendInfix(code, parentPrecedence,  "+", 1)
            is F64.Sub<*> -> appendInfix(code, parentPrecedence, "-", 1)
            is F64.Mul<*> -> appendInfix(code, parentPrecedence, "*", 2)
            is F64.Div<*> -> appendInfix(code, parentPrecedence, "/", 2)
            is Control.If<*> -> appendIf(code as Control.If<RuntimeContext>)
            is Control.Block<*> -> appendBlock(code as Control.Block<RuntimeContext>)
            is Control.While<*> -> appendWhile(code as Control.While<RuntimeContext>)
            else -> append(code.toString())
        }
        return this
    }

    fun appendInfix(code: Evaluable<*>, parentPrecedence: Int, name: String, precedence: Int) {
        if (parentPrecedence > precedence) {
            sb.append(')')
            appendInfix(code, precedence, name, precedence)
            sb.append(')')
        } else {
            appendCode(code.children()[0], precedence)
            append(" $name ")
            appendCode(code.children()[1], precedence)
        }
    }

    fun appendIf(expr: Control.If<RuntimeContext>): CodeWriter {
        append("if ")
        appendCode(expr.ifThenElse[0])
        append(':').indent().newline()
        appendCode(expr.ifThenElse[1])
        outdent()

        for (i in 2 until expr.ifThenElse.size - 1 step 2) {
            newline().append("elif ").indent()
            appendCode(expr.ifThenElse[i])
            append(':').newline()
            appendCode(expr.ifThenElse[i + 1])
            outdent()
        }
        if (expr.ifThenElse.size % 2 == 1) {
            newline().append("else:").indent().newline()
            appendCode(expr.ifThenElse[expr.ifThenElse.size - 1])
            outdent()
        }
        return this
    }

    fun appendWhile(expr: Control.While<RuntimeContext>): CodeWriter {
        append("while ")
        appendCode(expr.condition)
        append(':').indent().newline()
        appendCode(expr.body)
        outdent()
        return this
    }

    fun appendBlock(block: Control.Block<RuntimeContext>): CodeWriter {
        if (block.statements.size > 0) {
            appendCode(block.statements[0])
            for (i in 1 until block.statements.size) {
                newline()
                appendCode(block.statements[i])
            }
        }
        return this
    }


}