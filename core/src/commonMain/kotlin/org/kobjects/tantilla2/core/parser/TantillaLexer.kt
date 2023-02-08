package org.kobjects.tantilla2.core.parser

import org.kobjects.parserlib.tokenizer.Lexer
import org.kobjects.parserlib.tokenizer.RegularExpressions
import org.kobjects.parserlib.tokenizer.Token

class TantillaLexer(input: String, offset: Token<TokenType>? = null) : Iterator<Token<TokenType>> {

    val inner = Lexer(
        input,
        Regex("[ \\t\\r]+") to { null },
        RegularExpressions.NUMBER to { TokenType.NUMBER },
        Regex("\\+=|-=|\\*=|\\*\\*=|/=|//=|&=|\\|=|%=|>>=|<<=|\\^="
                + "|<=|<\\||<<|<|>=|>>|>|==|=|!=|->"
                + "|-|\\+|\\*\\*|\\*|%|&|\\||//|/|,|\\^|\\.|::|:")
                 to { TokenType.SYMBOL_INFIX },
        Regex("\\(|\\[|\\{|!|~") to { TokenType.SYMBOL_PREFIX },
        Regex("]|\\}|\\)|@") to { TokenType.SYMBOL_POSTFIX },
        RegularExpressions.IDENTIFIER to { when (it) {
            "and", "or", "in", "as" -> TokenType.SYMBOL_INFIX
            "not" -> TokenType.SYMBOL_PREFIX
            else -> TokenType.IDENTIFIER
        } },
        Regex("(\\n[ \\t]*)+") to { TokenType.LINE_BREAK },
            Regex("\"\"\"(.|\\n)*?\"\"\""
                    +"|'''(.|\\n)*?'''") to { TokenType.MULTILINE_STRING },
            RegularExpressions.DOUBLE_QUOTED_STRING to { TokenType.STRING },
            Regex("###(.|\\n)*?###") to { TokenType.DISABLED_CODE },
            Regex("#[^\\n]*") to { TokenType.COMMENT },
        Regex("\\S+") to { TokenType.UNRECOGNIZED },
        offset = offset
    )

    var previous: Token<TokenType>? = null
    var next: Token<TokenType>? = null
    var nextNext: Token<TokenType>? = null

    fun check() : Boolean =
        if (next == null) {
            false
        } else if (next?.type != TokenType.LINE_BREAK ) {
            true
        } else !(previous?.type == TokenType.SYMBOL_PREFIX
                || previous?.type == TokenType.SYMBOL_INFIX
                || nextNext?.type == TokenType.SYMBOL_INFIX
                || nextNext?.type == TokenType.SYMBOL_POSTFIX)


    override fun hasNext(): Boolean {
        if (next == null) {
            do {
                previous = next
                next = nextNext;
                if (inner.hasNext()) {
                    nextNext = inner.next()
                } else if (next == null) {
                    return false
                } else {
                    nextNext = null
                }
            } while (!check())
        }
        return true
    }

    override fun next(): Token<TokenType> {
        if (!hasNext()) {
            throw IllegalStateException("End of input exceeded.")
        }
        return next!!.apply { next = null }
    }

}