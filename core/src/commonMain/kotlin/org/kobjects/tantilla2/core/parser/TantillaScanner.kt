package org.kobjects.tantilla2.core.parser

import org.kobjects.parserlib.tokenizer.Scanner
import org.kobjects.parserlib.tokenizer.Lexer
import org.kobjects.parserlib.tokenizer.RegularExpressions

enum class TokenType {
   IDENTIFIER, NUMBER, SYMBOL_INFIX, SYMBOL_PREFIX, SYMBOL_POSTFIX, EOF, LINE_BREAK, STRING, MULTILINE_STRING, COMMENT, DISABLED_CODE
}

fun unquote(s: String): String {
    if (s.startsWith("\"\"\"")) {
        return s.substring(3, s.length -3)
    }

    return s.substring(1, s.length - 1)
}

class TantillaScanner(val code: String) : Scanner<TokenType>(TantillaLexer(code), TokenType.EOF)




