package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.node.LeafNode
import org.kobjects.tantilla2.core.type.Type

class TraitMethodBody(val vmtIndex: Int): LeafNode() {
    override val returnType: Type
        get() = throw UnsupportedOperationException()

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.append("<TraitMethodBody>")
    }

    override fun eval(context: LocalRuntimeContext) = TraitDefinition.evalMethod(context, vmtIndex)
}