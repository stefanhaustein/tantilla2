package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.builtin.StrType
import org.kobjects.tantilla2.core.node.Evaluable
import org.kobjects.tantilla2.core.classifier.Adapter

class TraitMethodBody(val index: Int): Evaluable {
    override val returnType: Type
        get() = throw UnsupportedOperationException()

    override fun eval(context: LocalRuntimeContext): Any? {
      val self = context.variables[0] as Adapter
      val methodImpl = self.vmt[index]

      val methodContext = LocalRuntimeContext(context.globalRuntimeContext,
          methodImpl.scopeSize, {
              if (it == 0) self.instance
              else if (it < context.variables.size) context.variables[it]
              else null
          }, methodImpl.closure)
      return self.vmt[index].eval(methodContext)
    }

    override fun children(): List<Evaluable> = emptyList()

    override fun reconstruct(newChildren: List<Evaluable>) = this
}