package org.kobjects.tantilla2.android.stdlib

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import org.kobjects.tantilla2.stdlib.BitmapImage
import org.kobjects.tantilla2.stdlib.Color

class BitmapImageImpl(
    val bitmap: Bitmap,
    val updateNotifier: MutableState<Int>,
) : BitmapImage {

    override val width: Int
        get() = bitmap.width
    override val height: Int
        get() = bitmap.height

    override fun set(x: Int, y: Int, color: Color) {
       bitmap.setPixel(x, y, color.argb)
        updateNotifier.value++
    }

    override fun get(x: Int, y: Int) =
        Color.argb(bitmap.getPixel(x, y))

}