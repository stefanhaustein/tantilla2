package org.kobjects.tantilla2.core.parser

import org.kobjects.parserlib.tokenizer.Tokenizer
import org.kobjects.parserlib.tokenizer.RegularExpressions

enum class TokenType {
    BOF, IDENTIFIER, NUMBER, SYMBOL, EOF, LINE_BREAK, STRING, MULTILINE_STRING, COMMENT, DISABLED_CODE
}

fun unquote(s: String): String {
    if (s.startsWith("\"\"\"")) {
        return s.substring(3, s.length -3)
    }

    return s.substring(1, s.length - 1)

}

class TantillaTokenizer(input: String) : Tokenizer<TokenType>(
    input,
    TokenType.BOF,
    TokenType.EOF,
    Regex("(\\n[ \\t]*)+") to TokenType.LINE_BREAK,
    Regex("[ \\t\\r]+") to null,
    RegularExpressions.IDENTIFIER to TokenType.IDENTIFIER,
    RegularExpressions.NUMBER to TokenType.NUMBER,
    Regex("\"\"\".*\"\"\"") to TokenType.MULTILINE_STRING,
    RegularExpressions.DOUBLE_QUOTED_STRING to TokenType.STRING,
    Regex("\\+|->|-|\\*\\*|\\*|%|//|/|<=|<\\||>=|==|=|<|>|\\^|!=|!|\\(|\\)|\\[|]|:|,|\\.") to TokenType.SYMBOL,
    Regex("###(.|\\n)*###") to TokenType.DISABLED_CODE,
    Regex("#[^\\n]*") to TokenType.COMMENT,
)



