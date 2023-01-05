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

class TantillaTokenizer(input: String, normalize: Boolean = true) : Tokenizer<TokenType>(
    input,
    TokenType.BOF,
    TokenType.EOF,
    Regex("[ \\t\\r]+") to null,
    RegularExpressions.NUMBER to TokenType.NUMBER,
    Regex("[ \\t\\r\\n]+(and|or|as|in)[ \\t\\r\\n]+") to TokenType.SYMBOL,
    Regex("not[ \\t\\r\\n]+") to TokenType.SYMBOL,
    Regex("[ \\t\\r\\n]*"
            + "(\\+=|-=|\\*=|\\*\\*=|/=|//=|&=|\\|=|%=|>>=|<<=|\\^="
            + "|<=|<\\||<<|<|>=|>>|>|==|=|!=|->"
            + "|-|\\+|\\*\\*|\\*|%|&|\\||//|/|,|\\^|\\."
            + ")[ \\t\\r\\n]*") to TokenType.SYMBOL,
    Regex("(\\(|\\[|\\{|!|~|::)[ \\t\\n\\r]*") to TokenType.SYMBOL,
    Regex("[ \\t\\n\\r]*(]|\\}|:|\\))") to TokenType.SYMBOL,
    Regex("(\\n[ \\t]*)+") to TokenType.LINE_BREAK,
    RegularExpressions.IDENTIFIER to TokenType.IDENTIFIER,
    Regex("\"\"\"(.|\\n)*?\"\"\""
            +"|'''(.|\\n)*?'''") to TokenType.MULTILINE_STRING,
    RegularExpressions.DOUBLE_QUOTED_STRING to TokenType.STRING,
    Regex("###(.|\\n)*?###") to TokenType.DISABLED_CODE,
    Regex("#[^\\n]*") to TokenType.COMMENT,
    normalization = { t, s -> if (t == TokenType.LINE_BREAK || !normalize) s else s.trim() }

)



