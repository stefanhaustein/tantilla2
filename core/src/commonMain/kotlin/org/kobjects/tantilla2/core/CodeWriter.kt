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
    var depth = 0

    fun addError(position: IntRange, error: Exception) {
        startIndices.put(pos + position.start, error)
        endIndices.add(pos + position.endInclusive + 1)
    }

    fun appendOpen(value: Char) {
        append(value)
        depth++
    }

    fun appendClose(value: Char) {
        append(value)
        depth--
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

    fun mark(unrestrictedLineLength: Boolean = true): Mark {
        val result = Mark()
        if (unrestrictedLineLength) {
            lineLength = Int.MAX_VALUE
        }
        return result
    }

    fun reset(mark: Mark) {
        unmark(mark)
        sb.setLength(mark.savedLength)
        pos = mark.savedPos
        depth = mark.savedDepth
    }

    fun unmark(mark: Mark) {
        lineLength = mark.savedLineLength
    }

    fun appendInParens(node: Node) {
        appendOpen('(')
        indent()
        appendCode(node)
        outdent()
        appendClose(')')
    }

    fun appendMaybeNextLine(node: Node) {
        val mark = mark()
        append(' ')
        appendCode(node)
        unmark(mark)
        if (x >= lineLength) {
            reset(mark)
            indent()
            newline()
            appendCode(node)
            outdent()
        }
    }

    fun appendList(expressions: List<Node>, prefixes: List<String>? = null) {
        val mark = mark()

        for (i in expressions.indices) {
            val node = expressions[i]
            if (sb.length > mark.savedLength) {
                append(", ")
            }
            if (prefixes != null) {
                append(prefixes[i])
            }
            appendCode(node)
        }

        unmark(mark)

        if (x + 1 >= lineLength) {
            val len = x - mark.savedX
            var multiLine = indent.length + 2 + len + 1 >= mark.savedLineLength

            reset(mark)
            indent()
            for (i in expressions.indices) {
                  if (sb.length == mark.savedLength) {
                      newline()
                  } else if (multiLine) {
                      sb.append(",")
                      newline()
                  } else {
                      sb.append(", ")
                  }
                  if (prefixes != null) {
                      append(prefixes[i])
                  }
                  appendCode(expressions[i])
              }
              outdent()
          }
        if (x >= lineLength - 1) {
            newline()
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
            appendInParens(code)
        } else {
            val outerMark = mark(false)
            appendCode(code.children()[0], precedence)
            val innerMark = mark(true)
            when (name) {
                "*", "**", "/", "//" -> append(name)
                else -> append(" $name ")
            }
            appendCode(code.children()[1], precedence + 1)
            unmark(outerMark)
            if (x >= lineLength) {
                if (depth > 0) {
                    reset(innerMark)
                    newline()
                    append("$name ")
                    appendCode(code.children()[1], precedence + 1)
                } else {
                    reset(outerMark)
                    appendInfix(code, precedence + 1, name, precedence)
                }
            }
        }
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


    inner class Mark() {
        val savedLength = sb.length
        val savedPos = pos
        val savedLineLength = lineLength
        val savedX = x
        val savedDepth = depth
    }
}