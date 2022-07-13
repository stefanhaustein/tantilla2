package org.kobjects.dialog

import androidx.annotation.FloatRange

class InputLine<T> (
    val label: String = "",
    val value: T,
    val options: Map<T, String>? = null,
    val range: ClosedFloatingPointRange<Float>? = null,
    val steps: Int = 0
)