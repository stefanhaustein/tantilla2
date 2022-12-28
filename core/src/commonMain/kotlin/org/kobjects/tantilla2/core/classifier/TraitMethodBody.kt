package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.classifier.Adapter
import org.kobjects.tantilla2.core.node.LeafNode
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.VoidType

class TraitMethodBody(val index: Int): LeafNode() {
    override val returnType: Type
        get() = throw UnsupportedOperationException()

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.append("<TraitMethodBody>")
    }

    override fun eval(context: LocalRuntimeContext): Any {
      val self = context.variables[0] as Adapter
      val methodImpl = self.vmt[index]

      val methodContext = LocalRuntimeContext(context.globalRuntimeContext,
          methodImpl, {
              if (it == 0) self.instance
              else if (it < context.variables.size) context.variables[it]
              else VoidType.None
          })
      return self.vmt[index].eval(methodContext)
    }
}