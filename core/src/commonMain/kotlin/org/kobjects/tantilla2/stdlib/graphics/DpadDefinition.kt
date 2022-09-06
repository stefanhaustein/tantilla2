package org.kobjects.tantilla2.stdlib.graphics

import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.builtin.Bool
import org.kobjects.tantilla2.core.builtin.RootScope

object DpadDefinition : NativeStructDefinition(
    RootScope,
"Dpad",
    docString = "Diretional pad."
) {

    init {
        BitmapImageDefinition.defineNativeProperty(
            "a",
            "True if the 'A'-key is pressed.",
            Bool,
            { (it as Dpad).a })

        BitmapImageDefinition.defineNativeProperty(
            "b",
            "True if the 'B'-key is pressed.",
            Bool,
            { (it as Dpad).b })

        BitmapImageDefinition.defineNativeProperty(
            "left",
            "True if the 'left'-key is pressed.",
            Bool,
            { (it as Dpad).left })

        BitmapImageDefinition.defineNativeProperty(
            "right",
            "True if the 'right'-key is pressed.",
            Bool,
            { (it as Dpad).right })

        BitmapImageDefinition.defineNativeProperty(
            "up",
            "True if the 'up'-key is pressed.",
            Bool,
            { (it as Dpad).up })

        BitmapImageDefinition.defineNativeProperty(
            "down",
            "True if the 'down'-key is pressed.",
            Bool,
            { (it as Dpad).down })

        BitmapImageDefinition.defineNativeProperty(
            "show",
            "True if the Dpad is visible.",
            Bool,
            { (it as Dpad).show })
    }
}