package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.function.Lambda

class LambdaReference(val lambda: Lambda) : TantillaNode {
    override val returnType: Type
        get() = lambda.type

    override fun children(): List<Evaluable<RuntimeContext>> = emptyList()

    override fun eval(context: RuntimeContext) = lambda

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) = this

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.appendCode(lambda, precedence)
    }
}