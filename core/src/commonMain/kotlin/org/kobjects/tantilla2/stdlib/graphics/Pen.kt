package org.kobjects.tantilla2.stdlib.graphics

interface Pen {
    var fillColor: Color
    var strokeColor: Color

    fun line(startX: Double, startY: Double, endX: Double, endY: Double)
    fun rect(x: Double, y: Double, width: Double, height: Double)
    fun circle(cx: Double, cy: Double, r: Double)
    fun text(x: Double, y: Double, text: String)
}