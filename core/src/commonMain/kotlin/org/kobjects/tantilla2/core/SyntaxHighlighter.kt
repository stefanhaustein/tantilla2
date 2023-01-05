package org.kobjects.tantilla2.core

import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.parser.TantillaTokenizer
import org.kobjects.tantilla2.core.parser.TokenType

fun highlightSyntax(
    writer: CodeWriter,
    code: String,
    errors: List<Throwable> = listOf(),
    runtimeException: Throwable? = null,
    runtimeExceptionPosition: IntRange = IntRange.EMPTY,
) {
    for (error in errors) {
        if (error is ParsingException) {
            writer.addError(IntRange(error.token.pos, error.token.pos + error.token.text.length - 1), error)
        }
    }
    if (runtimeException != null) {
        writer.addError(runtimeExceptionPosition, runtimeException)
    }




        val tokenizer = TantillaTokenizer(code, false)
        var wasDecl = false
        var lastPos = 0

        while (tokenizer.hasNext()) {
            val token = try {
                tokenizer.next()
            } catch (e: Exception) {
                e.printStackTrace()
                writer.append(code.substring(lastPos))
                return
            }
            if (token.pos > lastPos) {
                writer.append(code.subSequence(lastPos, token.pos))
            }


                when (token.type) {
                    TokenType.EOF,
                    TokenType.BOF -> {
                    }
                    TokenType.MULTILINE_STRING,
                    TokenType.STRING ->
                        writer.appendWrapped(CodeWriter.Kind.STRING, token.text)
                    TokenType.IDENTIFIER ->
                        when (token.text) {
                            "def", "for", "impl", "let", "mut", "struct", "var" -> {
                                writer.appendKeyword(token.text)
                                wasDecl = true
                            }
                            "else", "elif", "if", "in", "while" -> {
                                writer.appendKeyword(token.text)
                                wasDecl = false
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

}