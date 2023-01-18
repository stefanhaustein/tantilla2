package org.kobjects.tantilla2.core.parser

import org.kobjects.parserlib.tokenizer.Scanner
import org.kobjects.parserlib.tokenizer.Lexer
import org.kobjects.parserlib.tokenizer.RegularExpressions
import org.kobjects.parserlib.tokenizer.Token

enum class TokenType {
   IDENTIFIER, NUMBER, SYMBOL_INFIX, SYMBOL_PREFIX, SYMBOL_POSTFIX, EOF, LINE_BREAK, STRING, MULTILINE_STRING, COMMENT, DISABLED_CODE, UNRECOGNIZED
}


class TantillaScanner(val code: String, startPos: Token<TokenType>? = null) : Scanner<TokenType>(TantillaLexer(code, offset = startPos), TokenType.EOF)




