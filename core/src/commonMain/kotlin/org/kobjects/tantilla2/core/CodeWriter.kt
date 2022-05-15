package org.kobjects.tantilla2.core

import org.kobjects.konsole.Ansi

class CodeWriter(indent: String = "") : Appendable {
    val sb = StringBuilder()
    val indent = StringBuilder(indent)

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
}