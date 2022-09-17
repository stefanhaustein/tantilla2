package org.kobjects.tantilla2.stdlib.graphics

interface GraphicsSystem {
    fun createBitmap(width: Int, height: Int): BitmapImage

    fun image(): BitmapImage

    fun requestAnimationFrame(callback: () -> Unit)

    val screenWidth: Int

    val screenHeight: Int
}