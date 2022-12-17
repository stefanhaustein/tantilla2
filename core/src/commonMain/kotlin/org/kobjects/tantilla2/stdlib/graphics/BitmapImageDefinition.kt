package org.kobjects.tantilla2.stdlib.graphics

import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.FloatType
import org.kobjects.tantilla2.core.type.IntType
import org.kobjects.tantilla2.core.type.VoidType

class BitmapImageDefinition(val graphicsScope: GraphicsScope) : NativeStructDefinition(
    graphicsScope,
    "BitmapImage",
    docString = "Bitmap image representation",
    listOf(Parameter("width", IntType), Parameter("height", IntType)),
    { graphicsScope.graphicsSystem.createBitmap(it.i32(0), it.i32(1)) }
) {

    init {
        defineNativeProperty(
            "width",
            "The width of the image in pixels.",
            IntType,
             { (it as BitmapImage).width.toLong() })

        defineNativeProperty(
            "height",
            "The height of the image in pixels.",
            IntType,
            { (it as BitmapImage).height.toLong() })

        defineNativeFunction(
            "set",
            "Sets the pixel at the given coordinates to the given color",
            VoidType,
            Parameter("self", this),
            Parameter("x", FloatType),
            Parameter("y", FloatType),
            Parameter("color", graphicsScope.colorDefinition)
        ) {
            val image = it[0] as BitmapImage
            image[it.i32(1), it.i32(2)] = it[3] as Color
            VoidType.None
        }


        defineNativeFunction(
            "get",
            "Gets the pixel color at the given coordinates",
            graphicsScope.colorDefinition,
            Parameter("self", this),
            Parameter("x", FloatType),
            Parameter("y", FloatType),
        ) {
            val image = it[0] as BitmapImage
            image[it.i32(1), it.i32(2)]
        }

    }

}