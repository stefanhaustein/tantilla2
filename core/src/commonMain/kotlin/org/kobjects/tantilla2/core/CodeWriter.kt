package org.kobjects.tantilla2.core

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