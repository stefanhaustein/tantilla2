package org.kobjects.tantilla2.parser

import org.kobjects.parserlib.tokenizer.Tokenizer
import org.kobjects.parserlib.tokenizer.RegularExpressions

enum class TokenType {
    BOF, IDENTIFIER, NUMBER, SYMBOL, EOF, LINE_BREAK, STRING
}

class TantillaTokenizer(input: String) : Tokenizer<TokenType>(
    input,
    TokenType.BOF,
    TokenType.EOF,
    Regex("(\\n[ \\t]*)+") to TokenType.LINE_BREAK,
    Regex("[ \\t\\r]+") to null,
    RegularExpressions.IDENTIFIER to TokenType.IDENTIFIER,
    RegularExpressions.NUMBER to TokenType.NUMBER,
    RegularExpressions.DOUBLE_QUOTED_STRING to TokenType.STRING,
    Regex("\\+|-|\\*|%|<=|<\\||>=|==|=|<|>|\\^|!|\\(|\\)|:") to TokenType.SYMBOL,
)


