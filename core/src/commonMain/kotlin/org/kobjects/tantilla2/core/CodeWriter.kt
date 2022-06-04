package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.*
import org.kobjects.konsole.Ansi
import org.kobjects.tantilla2.core.runtime.Str

class CodeWriter(
    indent: String = "",
    val highlighting: Map<Kind, Pair<String, String>> = emptyMap(),
) : Appendable {
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

    fun start(kind: Kind): CodeWriter {
        val s = highlighting[kind]
        if (s != null) {
            sb.append(s.first)
        }
        return this
    }

    fun wrapped(kind: Kind, s: String) = start(kind).append(s).end(kind)

    fun end(kind: Kind): CodeWriter {
        val s = highlighting[kind]
        if (s != null) {
            sb.append(s.second)
        }
        return this
    }

    fun keyword(name: String) = wrapped(Kind.KEYWORD, name)

    fun declaration(name: String) = wrapped(Kind.DECLARATION, name)

    fun newline(): CodeWriter {
        sb.append('\n').append(indent)
        return this
    }

    fun appendType(type: Type): CodeWriter {
        type.serializeType(this)
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

    enum class Kind {
        KEYWORD, DECLARATION
    }

    companion object {
        val defaultHighlighting = mapOf(
            Kind.KEYWORD to Pair(Ansi.BOLD, Ansi.NORMAL_INTENSITY),
            Kind.DECLARATION to Pair(Ansi.BOLD + Ansi.FOREGROUND_CYAN, Ansi.NORMAL_INTENSITY + Ansi.FOREGROUND_DEFAULT)
        )
    }
}