package org.kobjects.tantilla2.core.definition

import org.kobjects.parserlib.tokenizer.Token
import org.kobjects.tantilla2.core.parser.TokenType

data class CodeFragment(
    val startPos: Token<TokenType>,
    val code: String,
)
