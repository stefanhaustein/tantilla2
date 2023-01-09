fun String.tripleQuote() = "\"\"\"$this\"\"\""

fun String.quote(): String {
    val sb = StringBuilder()
    sb.append('"')
    for (c in this) {
        when (c) {
            '\t' -> sb.append("\\t")
            '\n' -> sb.append("\\n")
            '"' -> sb.append("\"\"")
            else -> sb.append(c)
        }
    }
    sb.append('"')
    return sb.toString()
}

fun Any.stringify() = when (this) {
    is String -> this
    else -> toString()
}

fun Any.toLiteral() = when (this) {
    is String -> quote()
    else -> toString()
}