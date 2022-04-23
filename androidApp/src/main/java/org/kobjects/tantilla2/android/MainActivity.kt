package org.kobjects.tantilla2.android

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.coroutineScope
import org.kobjects.greenspun.core.F64
import org.kobjects.greenspun.core.Void
import org.kobjects.konsole.compose.ComposeKonsole
import org.kobjects.konsole.compose.RenderKonsole
import org.kobjects.tantilla2.console.ConsoleLoop
import org.kobjects.tantilla2.core.RootScope
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunction
import org.kobjects.tantilla2.core.function.Parameter
import kotlin.math.sqrt


fun greet(): String {
    return "test"
}

class MainActivity : AppCompatActivity() {

    lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val konsole = ComposeKonsole()

        val config = resources.configuration

        bitmap = Bitmap.createBitmap(
            config.screenWidthDp, config.screenHeightDp, Bitmap.Config.ARGB_8888)

        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                bitmap.setPixel(x, y, x*y or 0x7f000000)
            }
        }

        setContent {
            Render(konsole)
        }

        val rootScope = RootScope()

        rootScope.defineValue(
            "setPixel",
            NativeFunction(
                FunctionType(
                    Void,
                    listOf(
                        Parameter("x", F64),
                        Parameter("y", F64),
                        Parameter("color", F64)
                    )
                )
            ) {
                bitmap.setPixel(
                    (it.variables[0] as Double).toInt(),
                    (it.variables[1] as Double).toInt(),
                    (it.variables[2] as Double).toInt())
            })


        lifecycle.coroutineScope.launchWhenCreated {
            ConsoleLoop(konsole, rootScope)
        }
    }



    @Composable
    fun Render(konsole: ComposeKonsole) {
        MaterialTheme(
            colors = LIGHT_COLORS
        ) {
            Column() {
                TopAppBar(
                    title = { Text(text = "Tantilla 2") })
                Box() {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Canvas",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                        )

                    RenderKonsole(konsole = konsole)
                }
            }
        }
    }


    companion object {
        val LIGHT_COLORS = Colors(
            primary = Color(0xff46a0ff),
            primaryVariant = Color(0xff0072cb),
            secondary = Color(0xffffa546),
            secondaryVariant = Color(0xffc77612),
            background = Color(0xffdddddd),
            surface = Color(0xffdddddd),
            error = Color(0xffff0000),
            onPrimary = Color(0xffffffff),
            onSecondary = Color(0xffffffff),
            onBackground = Color(0xff000000),
            onSurface = Color(0xffffffff),
            onError = Color(0xffffffff),
            isLight = true)
    }

}
