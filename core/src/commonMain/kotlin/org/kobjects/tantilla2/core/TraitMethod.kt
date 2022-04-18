package org.kobjects.tantilla2.core

class TraitMethod(override val type: FunctionType, val index: Int): Lambda {
    override fun eval(context: RuntimeContext): Any? {
      val vmt = context.variables[0] as List<Lambda>
      val ctx = context.variables[1] as RuntimeContext
      return vmt[index].eval(ctx)
    }
}