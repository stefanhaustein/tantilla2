package org.kobjects.tantilla2.core.node.statement

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.LocalVariableDefinition
import org.kobjects.tantilla2.core.type.VoidType
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.Type

class Let(val definition: LocalVariableDefinition, val type: Type, val typeIsExplicit: Boolean, val initializer: Node?) : Node() {
    override val returnType: Type
        get() = VoidType

    override fun children() = if (initializer == null) emptyList() else listOf(initializer)

    override fun eval(context: LocalRuntimeContext): Any? {
        if (initializer != null) {
            context.variables[definition.index] = initializer.eval(context)
        }
        return null
    }

    override fun reconstruct(newChildren: List<Node>) =
        Let(definition, type, typeIsExplicit, if (newChildren.isEmpty()) null else newChildren[0])


    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendKeyword("let ")
        writer.appendDeclaration(definition.name)
        if (typeIsExplicit) {
            writer.append(": ")
            writer.appendType(type, null)
        }
        if (initializer != null) {
            writer.append(" = ")
            writer.appendCode(initializer)
        }
    }
}