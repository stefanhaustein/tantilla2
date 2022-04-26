package org.kobjects.tantilla2.android

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import org.kobjects.tantilla2.core.runtime.RootScope
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunction
import org.kobjects.tantilla2.core.function.Parameter


fun greet(): String {
    return "test"
}

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val konsole = ComposeKonsole()

        val config = resources.configuration

        val bitmap = Bitmap.createBitmap(
            config.screenWidthDp, config.screenHeightDp, Bitmap.Config.ARGB_8888)

        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                bitmap.setPixel(x, y, x*y or 0x7f000000)
            }
        }

        val rootScope = RootScope()
        rootScope.defineNative(
            "setPixel",
             Void,
                        Parameter("x", F64),
                        Parameter("y", F64),
                        Parameter("color", F64)
            ) {
                bitmap.setPixel(
                    (it.variables[0] as Double).toInt(),
                    (it.variables[1] as Double).toInt(),
                    (it.variables[2] as Double).toInt())
            }

        val viewModel = TantillaViewModel(rootScope, konsole, bitmap)
        lifecycle.coroutineScope.launchWhenCreated {
            ConsoleLoop(konsole, rootScope)
        }

        setContent {
            MaterialTheme(
                colors = LIGHT_COLORS
            ) {
                Render(viewModel)
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
