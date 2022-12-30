package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.type.GenericType
import org.kobjects.tantilla2.core.classifier.StructDefinition
import org.kobjects.tantilla2.core.classifier.StructMetaType
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.Type

class GenericTypeNode(
    val base: Node,
    val typeParams: List<Type>
): Node() {
    override fun children() = listOf(base)

    override fun reconstruct(newChildren: List<Node>) = GenericTypeNode(newChildren[0], typeParams)

    override fun eval(context: LocalRuntimeContext) = returnType.wrapped

    override val returnType: StructMetaType
        get() = (((base.returnType as StructMetaType).wrapped as GenericType).create(typeParams) as StructDefinition).type

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendCode(base)
        writer.append("[")
        writer.appendType(typeParams[0])
        for (i in 1 until typeParams.size) {
            writer.append(", ")
            writer.appendType(typeParams[i])
        }
        writer.append("]")
    }

}