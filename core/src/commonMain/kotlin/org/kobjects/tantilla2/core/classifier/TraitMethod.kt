package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.classifier.Adapter
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType

class TraitMethod(override val type: FunctionType, val index: Int): Callable, Evaluable<RuntimeContext> {
    override fun eval(context: RuntimeContext): Any? {
      val self = context.variables[0] as Adapter
      val methodImpl = self.vmt[index]

      val methodContext = RuntimeContext(
          MutableList(methodImpl.scopeSize) {
              if (it == 0) self.instance
              else if (it < context.variables.size) context.variables[it]
              else null
          }, methodImpl.closure)
      return self.vmt[index].eval(methodContext)
    }

    override fun children(): List<Evaluable<RuntimeContext>> = emptyList()

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) = this
}