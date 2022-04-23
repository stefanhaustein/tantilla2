package org.kobjects.tantilla2.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.ui.graphics.Color

fun greet(): String {
    return "test"
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Render()
        }
    }



    @Composable
    fun Render() {
        MaterialTheme(
            colors = LIGHT_COLORS
        ) {
            TopAppBar(
                title = { Text(text = "Tantilla 2") })
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
