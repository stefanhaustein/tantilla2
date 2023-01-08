package org.kobjects.tantilla2.android

import android.widget.ImageButton
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.kobjects.konsole.compose.ComposeKonsole
import org.kobjects.tantilla2.android.model.TantillaViewModel


@Composable
fun RenderKonsole(viewModel: TantillaViewModel) {
    Box() {
        key(viewModel.graphicsUpdateTrigger.value) {
            Image(
                bitmap = viewModel.bitmap.asImageBitmap(),
                contentDescription = "Canvas",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
        }
        Column() {
            RenderAppBar(
                viewModel,
            )
            Box(Modifier.pointerInput(Unit) {
                detectTapGestures(onTap = {
                    viewModel.onTap(it.x.toDouble(), it.y.toDouble())
                })
            }) {
                org.kobjects.konsole.compose.RenderKonsole(konsole = viewModel.konsole)
            }
        }
    }
}
