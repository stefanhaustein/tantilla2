package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.runtime.Void

class Let(val definition: VariableDefinition, val type: Type, val typeIsExplicit: Boolean, val initializer: Evaluable<RuntimeContext>?) : TantillaNode {
    override val returnType: Type
        get() = Void

    override fun children() = if (initializer == null) emptyList() else listOf(initializer)

    override fun eval(context: RuntimeContext): Any? {
        if (initializer != null) {
            context.variables[definition.index] = initializer.eval(context)
        }
        return null
    }

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) =
        Let(definition, type, typeIsExplicit, if (newChildren.isEmpty()) null else newChildren[0])


    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.keyword("let ")
        writer.declaration(definition.name)
        if (typeIsExplicit) {
            writer.append(": ")
            writer.appendType(type)
        }
        if (initializer != null) {
            writer.append(" = ")
            writer.appendCode(initializer)
        }
    }
}