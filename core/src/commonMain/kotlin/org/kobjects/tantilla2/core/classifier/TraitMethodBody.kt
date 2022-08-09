package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.classifier.Adapter
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType

class TraitMethodBody(val index: Int):  Evaluable<LocalRuntimeContext> {
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

    override fun children(): List<Evaluable<LocalRuntimeContext>> = emptyList()

    override fun reconstruct(newChildren: List<Evaluable<LocalRuntimeContext>>) = this
}