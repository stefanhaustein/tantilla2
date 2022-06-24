package org.kobjects.tantilla2.android

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.coroutineScope
import org.kobjects.konsole.compose.ComposeKonsole
import org.kobjects.tantilla2.console.ConsoleLoop
import org.kobjects.tantilla2.core.runtime.hsl
import java.lang.Math.abs


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

        val h2 = bitmap.height / 2
        val w2 = bitmap.width / 2
        val gray = Color.Gray.toArgb()

        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val color: Int = if ((x == w2 || y == h2)
                    && (abs(x - w2) <= 100 && abs(x - w2) % 10 == 0)
                    && (abs(y - h2) <= 100 && abs(y - h2) % 10 == 0)) gray
             //   else if (x + y > w2 + h2 + 320 && x + y < w2 + h2 + 320 + 90)
              //      hsl((x + y - w2 - h2 - 320) * 4.0, 1.0, 0.5, 0.3)
                else 0
                bitmap.setPixel(x, y, color)
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
            MyTheme {
                Render(viewModel)
            }
        }
    }

    fun loadExample(name: String) =
        assets.open("examples/$name").bufferedReader().use { it.readText() }

    @Composable
    fun MyTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        content: @Composable () -> Unit
    ) {
        val colors = if (darkTheme && false) {
            DARK_COLORS
        } else {
            LIGHT_COLORS
        }
        MaterialTheme(
            colors = colors,
            content = content
        )
    }


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
        val DARK_COLORS = Colors(
            primary = Color(0xff46a0ff),
            primaryVariant = Color(0xff0072cb),
            secondary = Color(0xffffa546),
            secondaryVariant = Color(0xffc77612),
            background = Color(0xff000000),
            surface = Color(0xff111111),
            error = Color(0xffff0000),
            onPrimary = Color(0xffffffff),
            onSecondary = Color(0xffffffff),
            onBackground = Color(0xffffffff),
            onSurface = Color(0xffffffff),
            onError = Color(0xffffffff),
            isLight = false)
    }

}
