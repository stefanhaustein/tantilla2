package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.parser.TantillaTokenizer
import org.kobjects.tantilla2.core.parser.TokenType


fun highlightSyntax(code: String, highlighting: Map<CodeWriter.Kind, Pair<String, String>> ): String {
    try {
        val writer = CodeWriter(highlighting = highlighting)
        val tokenizer = TantillaTokenizer(code)
        var wasDecl = false
        var lastPos = 0

        while (tokenizer.hasNext()) {
            val token = tokenizer.next()

            if (token.pos > lastPos) {
                writer.append(code.subSequence(lastPos, token.pos))
            }

            when (token.type) {
                TokenType.EOF,
                TokenType.BOF -> {}
                TokenType.MULTILINE_STRING,
                TokenType.STRING ->
                    writer.appendWrapped(CodeWriter.Kind.STRING, token.text)
                TokenType.IDENTIFIER ->
                    when (token.text) {
                        "def", "var", "mut", "struct", "impl" -> {
                            writer.appendKeyword(token.text)
                            wasDecl = true
                        }
                        else -> {
                            if (wasDecl) {
                                writer.appendDeclaration(token.text)
                                wasDecl = false
                            } else {
                                writer.append(token.text)
                            }
                        }
                    }
                else -> writer.append(token.text)
            }

            lastPos = token.pos + token.text.length
        }
        if (lastPos < code.length) {
            writer.append(code.substring(lastPos))
        }
        println(writer.toString())
        return writer.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        return code
    }
}