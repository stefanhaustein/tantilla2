package org.kobjects.tantilla2.android

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.runBlocking
import org.kobjects.konsole.compose.ComposeKonsole
import org.kobjects.tantilla2.android.model.Platform
import org.kobjects.tantilla2.android.model.TantillaViewModel
import org.kobjects.tantilla2.core.system.Lock
import org.kobjects.tantilla2.core.system.ThreadHandle
import java.io.File
import java.lang.Math.abs



class MainActivity : AppCompatActivity() {

    lateinit var viewModel: TantillaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val console = ComposeKonsole()
        val platform = AndroidPlatform(
            printImpl = { console.write(it) },
            inputImpl = { label ->
                var result: String
                runBlocking {
                    result = console.read(label)
                }
                result
            })


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

        viewModel = TantillaViewModel(
            console,
            bitmap,
            platform,
        )

        Thread { viewModel.consoleLoop() }.start()

        setContent {
            MyTheme {
                Render(viewModel)
            }
        }
    }

    override fun onBackPressed() {
        if (viewModel.navigationStack.size < 2) {
            super.onBackPressed()
        } else {
            viewModel.navigateBack(viewModel.navigationStack[viewModel.navigationStack.size - 2])
        }
    }


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
            typography = TYPOGRAPHY,
            content = content
        )
    }



    inner class AndroidPlatform(
        val printImpl: (String) -> Unit,
        val inputImpl: (String?) -> String,
    ) : Platform {

        override val rootDirectory: File
            get() = filesDir

        override fun loadExample(name: String) =
            assets.open("examples/$name").bufferedReader().use { it.readText() }

        override var fileName: String
            get() = getPreferences(0).getString("filename", "Scratch.tt")!!
            set(value) = getPreferences(0).edit().putString("filename", value).apply()



        override fun write(s: String) {
            printImpl(s)
        }

        override fun launch(task: (ThreadHandle) -> Unit): ThreadHandle {
            val thread = Thread {
                task(object : ThreadHandle {
                    val self = Thread.currentThread()
                    override fun cancel() {
                        self.interrupt()
                    }
                })
            }
            thread.start()
            return object : ThreadHandle {
                override fun cancel() {
                    thread.interrupt()
                }

            }
        }

        override fun createLock(): Lock {
            TODO("Not yet implemented")
        }

        override fun input(label: String?): String = inputImpl(label)

    }


    companion object {
        val TYPOGRAPHY = Typography(
            defaultFontFamily = FontFamily(
                Font(R.font.roboto_condensed_regular),
                Font(R.font.roboto_condensed_bold, weight = FontWeight.Bold)
            )
        )

        val LIGHT_COLORS = Colors(
            primary = Color(0xff555555), // Palette.BLUE),
            primaryVariant = Color(0xff0072cb),
            secondary = Color(0xfff49200),
            secondaryVariant = Color(0xffb06000),
            background = Color(0xffffffff),
            surface = Color(0xffe8e9ea),
            error = Color(0xffeb586e),
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
