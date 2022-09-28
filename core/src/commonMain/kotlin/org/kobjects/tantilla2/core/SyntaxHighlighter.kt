package org.kobjects.tantilla2.core

import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.parser.TantillaTokenizer
import org.kobjects.tantilla2.core.parser.TokenType




fun highlightSyntax(
    writer: CodeWriter,
    code: String,
    errors: List<Exception> = listOf(),
    runtimeException: Exception? = null,
    runtimeExceptionPosition: IntRange = IntRange.EMPTY,
) {
    val map = mutableMapOf<IntRange, Exception>()
    for (error in errors) {
        if (error is ParsingException) {
            map.put(IntRange(error.token.pos, error.token.pos + error.token.text.length), error)
        }
    }
    if (runtimeException != null) {
        map.put(runtimeExceptionPosition, runtimeException)
    }

    // Just start positions for now
    val errorMap = map.keys.associateBy { it.first }



        val tokenizer = TantillaTokenizer(code)
        var wasDecl = false
        var lastPos = 0

        while (tokenizer.hasNext()) {
            val token = try {
                tokenizer.next()
            } catch (e: Exception) {
                writer.append(code.substring(lastPos))
                return
            }
            if (token.pos > lastPos) {
                writer.append(code.subSequence(lastPos, token.pos))
            }

            if (errorMap.containsKey(token.pos)) {
                writer.appendWrapped(CodeWriter.Kind.ERROR, token.text)
            } else {

                when (token.type) {
                    TokenType.EOF,
                    TokenType.BOF -> {
                    }
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
            }
            lastPos = token.pos + token.text.length
        }
        if (lastPos < code.length) {
            writer.append(code.substring(lastPos))
        }

}