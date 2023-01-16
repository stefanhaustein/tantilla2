package org.kobjects.tantilla2.core.parser

fun String.unquote() = this.substring(1, this.length - 1)

fun String.unquoteMultiline() = this.substring(3, this.length - 3)

fun String.unescape(): String {
    val sb = StringBuilder()
    var escaped = false
    for (c in this) {
        if (escaped) {
            when (c) {
                'n' -> sb.append('\n')
                'r' -> sb.append('\r')
                't' -> sb.append('\t')
                '\\' -> sb.append('\\')
                else -> throw IllegalArgumentException("Unsupported escape: $c")
            }
            escaped = false
        } else if (c == '\\') {
            escaped = true
        } else {
            sb.append(c)
        }
    }
    if (escaped) {
        throw IllegalArgumentException("Unterminated escape sequence.")
    }
    return sb.toString()
}