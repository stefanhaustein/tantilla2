package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.builtin.RootScope
import org.kobjects.tantilla2.core.function.LocalVariableDefinition
import org.kobjects.tantilla2.core.builtin.VoidType

class Let(val definition: LocalVariableDefinition, val type: Type, val typeIsExplicit: Boolean, val initializer: Evaluable<LocalRuntimeContext>?) : TantillaNode {
    override val returnType: Type
        get() = VoidType

    override fun children() = if (initializer == null) emptyList() else listOf(initializer)

    override fun eval(context: LocalRuntimeContext): Any? {
        if (initializer != null) {
            context.variables[definition.index] = initializer.eval(context)
        }
        return null
    }

    override fun reconstruct(newChildren: List<Evaluable<LocalRuntimeContext>>) =
        Let(definition, type, typeIsExplicit, if (newChildren.isEmpty()) null else newChildren[0])


    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.appendKeyword("let ")
        writer.appendDeclaration(definition.name)
        if (typeIsExplicit) {
            writer.append(": ")
            writer.appendType(type, RootScope)
        }
        if (initializer != null) {
            writer.append(" = ")
            writer.appendCode(initializer)
        }
    }
}