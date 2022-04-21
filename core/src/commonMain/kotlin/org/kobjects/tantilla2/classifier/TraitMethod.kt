package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.classifier.Adapter
import org.kobjects.tantilla2.function.Callable
import org.kobjects.tantilla2.function.FunctionType

class TraitMethod(override val type: FunctionType, val index: Int): Callable {
    override fun eval(context: RuntimeContext): Any? {
      val self = context.variables[0] as Adapter
      val methodContext = RuntimeContext(
          MutableList(context.variables.size) { if (it == 0) self.instance else context.variables[it] })
      return self.vmt[index].eval(methodContext)
    }
}