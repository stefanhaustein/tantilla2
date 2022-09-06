package org.kobjects.tantilla2.stdlib.graphics

import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.builtin.F64
import org.kobjects.tantilla2.core.builtin.I64
import org.kobjects.tantilla2.core.builtin.RootScope
import org.kobjects.tantilla2.core.builtin.Void

object BitmapImageDefinition : NativeStructDefinition(
    RootScope,
    "BitmapImage",
    docString = "Bitmap image representation"
) {

    init {
        defineNativeProperty(
            "width",
            "The width of the image in pixels.",
            I64,
             { (it as BitmapImage).width.toLong() })

        defineNativeProperty(
            "height",
            "The height of the image in pixels.",
            I64,
            { (it as BitmapImage).height.toLong() })

        defineNativeFunction(
            "set",
            "Sets the pixel at the given coordinates to the given color",
            Void,
            Parameter("self", this),
            Parameter("x", F64),
            Parameter("y", F64),
            Parameter("color", ColorDefinition)
        ) {
            val image = it[0] as BitmapImage
            image[it.i32(1), it.i32(2)] = it[3] as Color
            null
        }


        defineNativeFunction(
            "get",
            "Gets the pixel color at the given coordinates",
            ColorDefinition,
            Parameter("self", this),
            Parameter("x", F64),
            Parameter("y", F64),
        ) {
            val image = it[0] as BitmapImage
            image[it.i32(1), it.i32(2)]
        }

    }

}