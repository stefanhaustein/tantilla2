package org.kobjects.tantilla2.android

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.coroutineScope
import org.kobjects.konsole.compose.ComposeKonsole
import org.kobjects.tantilla2.console.ConsoleLoop


fun greet(): String {
    return "test"
}

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val console = ConsoleLoop(ComposeKonsole())

        val config = resources.configuration

        val bitmap = Bitmap.createBitmap(
            config.screenWidthDp, config.screenHeightDp, Bitmap.Config.ARGB_8888)

        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                bitmap.setPixel(x, y, x*y or 0x7f000000)
            }
        }

        val viewModel = TantillaViewModel(
            console,
            bitmap,
            ::loadExample,
        )

        lifecycle.coroutineScope.launchWhenCreated {
            console.run()
        }

        setContent {
            MaterialTheme(
                colors = LIGHT_COLORS
            ) {
                Render(viewModel)
            }
        }
    }

    fun loadExample(name: String) =
        assets.open("examples/$name").bufferedReader().use { it.readText() }


    companion object {
        val LIGHT_COLORS = Colors(
            primary = Color(0xff46a0ff),
            primaryVariant = Color(0xff0072cb),
            secondary = Color(0xffffa546),
            secondaryVariant = Color(0xffc77612),
            background = Color(0xffffffff),
            surface = Color(0xffeeeeee),
            error = Color(0xffff0000),
            onPrimary = Color(0xffffffff),
            onSecondary = Color(0xffffffff),
            onBackground = Color(0xff000000),
            onSurface = Color(0xff000000),
            onError = Color(0xffffffff),
            isLight = true)
    }

}
