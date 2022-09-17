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


    private val bitmapImage = BitmapImageImpl(bitmap, graphicsUpdateTrigger)

    override fun createBitmap(width: Int, height: Int): BitmapImage {
        return BitmapImageImpl(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888), graphicsUpdateTrigger)
    }

    override fun image() = bitmapImage

    override fun requestAnimationFrame(callback: () -> Unit) {
        Choreographer.getInstance().postFrameCallback { callback() }
    }

}