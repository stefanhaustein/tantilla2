package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.*
import org.kobjects.konsole.Ansi

class CodeWriter(
    indent: String = "",
    val highlighting: Map<Kind, Pair<String, String>> = emptyMap(),
    val errorMap: Map<Evaluable<RuntimeContext>, TantillaRuntimeException> = emptyMap()
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

    fun stringLiteral(text: String) = wrapped(Kind.STRING, text)

    fun newline(): CodeWriter {
        sb.append('\n').append(indent)
        return this
    }

    fun appendType(type: Type): CodeWriter {
        type.serializeType(this)
        return this
    }

    fun appendCode(code: Any?, parentPrecedence: Int = 0): CodeWriter {
        val error = code is Evaluable<*> && errorMap.containsKey(code)
        if (error) {
            start(Kind.ERROR)
        }
        when (code) {
            is SerializableCode -> code.serializeCode(this, parentPrecedence)
            is F64.Gt<*> -> appendInfix(code, parentPrecedence,">", 2)
            is F64.Ge<*> -> appendInfix(code, parentPrecedence,">=", 2)
            is F64.Lt<*> -> appendInfix(code, parentPrecedence,"<", 2)
            is F64.Le<*> -> appendInfix(code, parentPrecedence,"<=", 2)
            is F64.Eq<*> -> appendInfix(code, parentPrecedence,"==", 2)
            is F64.Ne<*> -> appendInfix(code, parentPrecedence,"!=", 2)
            is F64.Add<*> -> appendInfix(code, parentPrecedence,  "+", 3)
            is F64.Sub<*> -> appendInfix(code, parentPrecedence, "-", 3)
            is F64.Mul<*> -> appendInfix(code, parentPrecedence, "*", 5)
            is F64.Div<*> -> appendInfix(code, parentPrecedence, "/", 5)
            is F64.Mod<*> -> appendInfix(code, parentPrecedence, "%", 5)
            is F64.Pow<*> -> appendInfix(code, parentPrecedence, "**", 6)
            is Control.If<*> -> appendIf(code as Control.If<RuntimeContext>)
            is Control.Block<*> -> appendBlock(code as Control.Block<RuntimeContext>)
            is Control.While<*> -> appendWhile(code as Control.While<RuntimeContext>)
            is Str.Const<*> -> wrapped(Kind.STRING, code.toString())
            else -> append(code.toString())
        }
        if (error) {
            end(Kind.ERROR)
        }
        return this
    }

    fun appendInfix(code: Evaluable<*>, parentPrecedence: Int, name: String, precedence: Int) {
        if (parentPrecedence > precedence) {
            sb.append('(')
            appendInfix(code, precedence, name, precedence)
            sb.append(')')
        } else {
            appendCode(code.children()[0], precedence)
            append(" $name ")
            appendCode(code.children()[1], precedence + 1)
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
        KEYWORD, DECLARATION, ERROR, STRING
    }

    companion object {
        val defaultHighlighting = mapOf(
            Kind.KEYWORD to Pair(Ansi.rgbForeground(0xdc7900), Ansi.FOREGROUND_DEFAULT),
            Kind.DECLARATION to Pair(Ansi.rgbForeground(0x3889c4), Ansi.FOREGROUND_DEFAULT),
            Kind.ERROR to Pair(Ansi.rgbBackground(0xeb586e), Ansi.BACKGROUND_DEFAULT),
            Kind.STRING to Pair(Ansi.rgbForeground(0x5c9238), Ansi.FOREGROUND_DEFAULT)
        )
        val darkThemeHighlighting = mapOf(
            Kind.KEYWORD to Pair(Ansi.rgbForeground(0xffae30), Ansi.FOREGROUND_DEFAULT),
            Kind.DECLARATION to Pair(Ansi.rgbForeground(0x3889c4), Ansi.FOREGROUND_DEFAULT),
            Kind.ERROR to Pair(Ansi.rgbBackground(0xeb586e), Ansi.BACKGROUND_DEFAULT),
            Kind.STRING to Pair(Ansi.rgbForeground(0xa7d489), Ansi.FOREGROUND_DEFAULT)
        )
    }
}