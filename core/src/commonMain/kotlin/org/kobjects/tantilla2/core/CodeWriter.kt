package org.kobjects.tantilla2.core

import org.kobjects.konsole.Ansi
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.node.*
import org.kobjects.tantilla2.core.type.Type
import quote
import tripleQuote

class CodeWriter(
    indent: String = "",
    val highlighting: Map<Kind, Pair<String, String>> = emptyMap(),
    val errorNode: Evaluable? = null,
    val errors: List<Pair<IntRange, Exception>> = emptyList(),
    var lineLength: Int = Int.MAX_VALUE,
) : Appendable {
    val sb = StringBuilder()
    val indent = StringBuilder(indent)
    var errorPosition = -1
    var errorLength = 0
    var pos = 0
    val startIndices = mutableMapOf<Int, Exception>()
    val endIndices = mutableSetOf<Int>()
    var lineStart = 0
    var spacePressure = false
    var lhs = false

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

    val x: Int
       get() = pos - lineStart

    fun mark() = Pair(sb.length, pos)

    fun reset(mark: Pair<Int, Int>) {
        sb.setLength(mark.first)
        pos = mark.second
    }


    fun appendList(expressions: List<Node>, prefixes: List<String>? = null) {
        val mark = mark()
        val savedLineLength = lineLength
        lineLength = Int.MAX_VALUE
        var ok = !spacePressure
        if (ok) {
        for (i in expressions.indices) {
            val node = expressions[i]
            if (sb.length > mark.first) {
                append(", ")
            }
            if (prefixes != null) {
                append(prefixes[i])
            }
            appendCode(node)
            if (x > savedLineLength) {
                ok = false
                break
            }
        }
        } else {
            spacePressure = false
        }
        lineLength = savedLineLength

        if (!ok) {
            reset(mark)
            var x0 = x
            indent()
            for (i in expressions.indices) {
                if (sb.length != mark.first) {
                    sb.append(",")
                    if (x > x0 + 2) {
                        newline()
                    } else {
                        sb.append(' ')
                    }
                } else {
                    newline()
                    x0 = x
                }
                if (prefixes != null) {
                    append(prefixes[i])
                }
                appendCode(expressions[i])
            }
            outdent()
            if (lhs) {
                newline()
            }
        }
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

    fun newline(): CodeWriter {
        append("\n")
                lineStart = pos
        append(indent)
        return this
    }

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
        if (code is SerializableCode) {
            code.serializeCode(this, parentPrecedence)
        } else {
            append(code.toString())
        }
        if (error) {
            errorLength = pos - errorPosition
            appendEnd(Kind.ERROR)
        }
        return this
    }

    fun appendQuoted(s: String) {
        appendWrapped(Kind.STRING, s.quote())
    }

    fun appendTripleQuoted(s: String) {
        appendWrapped(Kind.STRING, s.tripleQuote())
    }


    fun appendComment(s: String) {
        appendWrapped(Kind.COMMENT, s)
    }

    fun appendPrefix(code: Node, parentPrecedence: Int, name: String, precedence: Int) {
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

    fun appendInfix(code: Node, parentPrecedence: Int, name: String, precedence: Int) {
        if (parentPrecedence > precedence) {
            append('(')
            appendInfix(code, precedence, name, precedence)
            append(')')
        } else {
            val mark = mark()
            appendInfixImpl(code, parentPrecedence, name, precedence)
            if (x >= lineLength) {
                reset(mark)
                val savePressure = spacePressure
                spacePressure = true
                appendInfixImpl(code, parentPrecedence, name, parentPrecedence)
                spacePressure = savePressure
            }

        }
    }

    private fun appendInfixImpl(code: Node, parentPrecedence: Int,  name: String, precedence: Int) {
        val saveLhs = lhs
        lhs = true
        appendCode(code.children()[0], precedence)
        lhs = saveLhs
        when (name) {
            "*", "**", "/", "//" -> append(name)
            else -> append(" $name ")
        }
        appendCode(code.children()[1], precedence + 1)
    }


    fun appendUnparsed(code: String, errors: List<Exception> = emptyList()): CodeWriter {
        if (highlighting.isEmpty()) {
            append("### ")
            append(code.replace("###", "## "))
            append(" ###")
        } else {
            highlightSyntax(this, code, errors)
        }
        return this
    }

    enum class Kind {
        KEYWORD, DECLARATION, ERROR, STRING, COMMENT
    }

    companion object {


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