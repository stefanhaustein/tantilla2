package org.kobjects.tantilla2.core.runtime

import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.function.Parameter

object RootScope : Scope(null) {


    override val title: String
        get() = "Root Scope"

    init {


        F64.defineNativeFunction(
            "range", "Creates a range from start (inclusive) to end (exclusive)",
            RangeType,
            Parameter("start", F64),
            Parameter("end", F64)
        ) { Range(it.f64(0), it.f64(1)) }


        add(Definition(this, Definition.Kind.SCOPE,"math", resolvedValue = MathScope))

    }



}