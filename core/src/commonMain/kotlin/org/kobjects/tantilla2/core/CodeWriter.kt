package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.*
import org.kobjects.konsole.Ansi

class CodeWriter(
    indent: String = "",
    val highlighting: Map<Kind, Pair<String, String>> = emptyMap(),
    val errorNode: Evaluable<LocalRuntimeContext>? = null,
    val errors: List<Pair<IntRange, Exception>> = emptyList()
) : Appendable {
    val sb = StringBuilder()
    val indent = StringBuilder(indent)
    var errorPosition = -1
    var errorLength = 0
    var pos = 0
    val startIndices = mutableMapOf<Int, Exception>()
    val endIndices = mutableSetOf<Int>()

    fun addError(position: IntRange, error: Exception) {
        startIndices.put(pos + position.start, error)
        endIndices.add(pos + position.endInclusive + 1)
    }

    override fun append(value: Char): CodeWriter {
        if (startIndices.containsKey(pos)) {
            appendStart(Kind.ERROR)
        }
        sb.append(value)
        pos++
        if (endIndices.contains(pos)) {
            appendEnd(Kind.ERROR)
        }
        return this
    }

    override fun append(value: CharSequence?): CodeWriter = append(value, 0, value?.length ?: 0)

    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): CodeWriter {
        for (i in startIndex until endIndex) {
            append(value!![i])
        }
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

    fun appendStart(kind: Kind): CodeWriter {
        val s = highlighting[kind]
        if (s != null) {
            sb.append(s.first)
        }
        return this
    }

    fun appendWrapped(kind: Kind, s: String) = appendStart(kind).append(s).appendEnd(kind)

    fun appendEnd(kind: Kind): CodeWriter {
        val s = highlighting[kind]
        if (s != null) {
            sb.append(s.second)
        }
        return this
    }

    fun appendKeyword(name: String) = appendWrapped(Kind.KEYWORD, name)

    fun appendDeclaration(name: String) = appendWrapped(Kind.DECLARATION, name)

    fun newline(): CodeWriter = append("\n").append(indent)

    fun appendType(type: Type, scope: Scope?): CodeWriter {
        type.serializeType(this, scope)
        return this
    }

    fun appendCode(code: Any?, parentPrecedence: Int = 0): CodeWriter {
        val error = code == errorNode
        if (error) {
            errorPosition = pos
            appendStart(Kind.ERROR)
        }
        when (code) {
            is SerializableCode -> code.serializeCode(this, parentPrecedence)
            is I64.Neg<*> -> appendPrefix(code, parentPrecedence, "-", 4)
            is F64.Neg<*> -> appendPrefix(code, parentPrecedence, "-", 4)
            is I64.Gt<*> -> appendInfix(code, parentPrecedence,">", 2)
            is F64.Gt<*> -> appendInfix(code, parentPrecedence,">", 2)
            is I64.Ge<*> -> appendInfix(code, parentPrecedence,">=", 2)
            is F64.Ge<*> -> appendInfix(code, parentPrecedence,">=", 2)
            is I64.Lt<*> -> appendInfix(code, parentPrecedence,"<", 2)
            is F64.Lt<*> -> appendInfix(code, parentPrecedence,"<", 2)
            is I64.Le<*> -> appendInfix(code, parentPrecedence,"<=", 2)
            is F64.Le<*> -> appendInfix(code, parentPrecedence,"<=", 2)
            is F64.Eq<*> -> appendInfix(code, parentPrecedence,"==", 2)
            is I64.Eq<*> -> appendInfix(code, parentPrecedence,"==", 2)
            is F64.Ne<*> -> appendInfix(code, parentPrecedence,"!=", 2)
            is I64.Ne<*> -> appendInfix(code, parentPrecedence,"!=", 2)
            is I64.Add<*> -> appendInfix(code, parentPrecedence,  "+", 3)
            is F64.Add<*> -> appendInfix(code, parentPrecedence,  "+", 3)
            is Str.Add<*> -> appendInfix(code, parentPrecedence,  "+", 3)
            is I64.Sub<*> -> appendInfix(code, parentPrecedence, "-", 3)
            is F64.Sub<*> -> appendInfix(code, parentPrecedence, "-", 3)
            is I64.Mul<*> -> appendInfix(code, parentPrecedence, "*", 5)
            is F64.Mul<*> -> appendInfix(code, parentPrecedence, "*", 5)
            is I64.Div<*> -> appendInfix(code, parentPrecedence, "//", 5)
            is F64.Div<*> -> appendInfix(code, parentPrecedence, "/", 5)
            is I64.Mod<*> -> appendInfix(code, parentPrecedence, "%", 5)
            is F64.Mod<*> -> appendInfix(code, parentPrecedence, "%", 5)
            is F64.Pow<*> -> appendInfix(code, parentPrecedence, "**", 6)
            is Control.If<*> -> appendIf(code as Control.If<LocalRuntimeContext>)
            is Control.Block<*> -> appendBlock(code as Control.Block<LocalRuntimeContext>)
            is Control.While<*> -> appendWhile(code as Control.While<LocalRuntimeContext>)
            is Str.Const<*> -> appendWrapped(Kind.STRING, code.toString())
            else -> append(code.toString())
        }
        if (error) {
            errorLength = pos - errorPosition
            appendEnd(Kind.ERROR)
        }
        return this
    }

    fun appendTripleQuoted(s: String) {
        appendWrapped(Kind.STRING, tripleQuote(s))
    }

    fun appendComment(s: String) {
        appendWrapped(Kind.COMMENT, s)
    }

    fun appendPrefix(code: Evaluable<*>, parentPrecedence: Int, name: String, precedence: Int) {
        if (parentPrecedence > precedence) {
            append('(')
            append(name)
            appendCode(code.children()[0], precedence)
            append(')')
        } else {
            append(name)
            appendCode(code.children()[0], precedence)
        }
    }

    fun appendInfix(code: Evaluable<*>, parentPrecedence: Int, name: String, precedence: Int) {
        if (parentPrecedence > precedence) {
            append('(')
            appendInfix(code, precedence, name, precedence)
            append(')')
        } else {
            appendCode(code.children()[0], precedence)
            append(" $name ")
            appendCode(code.children()[1], precedence + 1)
        }
    }

    fun appendIf(expr: Control.If<LocalRuntimeContext>): CodeWriter {
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

    fun appendWhile(expr: Control.While<LocalRuntimeContext>): CodeWriter {
        append("while ")
        appendCode(expr.condition)
        append(':').indent().newline()
        appendCode(expr.body)
        outdent()
        return this
    }

    fun appendBlock(block: Control.Block<LocalRuntimeContext>): CodeWriter {
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
        KEYWORD, DECLARATION, ERROR, STRING, COMMENT
    }

    companion object {
        fun tripleQuote(s: String) = "\"\"\"$s\"\"\""


        val defaultHighlighting = mapOf(
            Kind.COMMENT to Pair(Ansi.rgbForeground(0x777777), Ansi.FOREGROUND_DEFAULT),
            Kind.KEYWORD to Pair(Ansi.rgbForeground(Palette.DARK_ORANGE.toInt()), Ansi.FOREGROUND_DEFAULT),
            Kind.DECLARATION to Pair(Ansi.rgbForeground(Palette.DARK_BLUE.toInt()), Ansi.FOREGROUND_DEFAULT),
            Kind.ERROR to Pair(Ansi.rgbBackground(Palette.BRIGHTEST_RED.toInt()), Ansi.BACKGROUND_DEFAULT),
            Kind.STRING to Pair(Ansi.rgbForeground(Palette.BRIGHT_GREEN.toInt()), Ansi.FOREGROUND_DEFAULT)
        )
        val darkThemeHighlighting = mapOf(
            Kind.KEYWORD to Pair(Ansi.rgbForeground(0xffae30), Ansi.FOREGROUND_DEFAULT),
            Kind.DECLARATION to Pair(Ansi.rgbForeground(0x3889c4), Ansi.FOREGROUND_DEFAULT),
            Kind.ERROR to Pair(Ansi.rgbBackground(0xeb586e), Ansi.BACKGROUND_DEFAULT),
            Kind.STRING to Pair(Ansi.rgbForeground(0xa7d489), Ansi.FOREGROUND_DEFAULT)
        )
    }
}