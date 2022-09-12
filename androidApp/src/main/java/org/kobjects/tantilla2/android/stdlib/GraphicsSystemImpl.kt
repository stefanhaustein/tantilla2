package org.kobjects.tantilla2.android.stdlib

import android.graphics.Bitmap
import android.view.Choreographer
import androidx.compose.runtime.MutableState
import org.kobjects.tantilla2.stdlib.graphics.BitmapImage
import org.kobjects.tantilla2.stdlib.graphics.GraphicsSystem

class GraphicsSystemImpl(
    bitmap: Bitmap,
    val graphicsUpdateTrigger: MutableState<Int>
) : GraphicsSystem {
    val tapListeners = mutableListOf<(Double, Double) -> Unit>()


    private val bitmapImage = BitmapImageImpl(bitmap, graphicsUpdateTrigger)

    override fun createBitmap(width: Int, height: Int): BitmapImage {
        return BitmapImageImpl(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888), graphicsUpdateTrigger)
    }

    override fun image() = bitmapImage

    override fun requestAnimationFrame(callback: () -> Unit) {
        Choreographer.getInstance().postFrameCallback { callback() }
    }

    fun onTap(x: Double, y: Double) {
        for (callback in tapListeners) {
            callback(x, y)
        }
    }

    override fun addTapListener(callback: (x: Double, y: Double) -> Unit) {
        tapListeners.add(callback)
    }
}