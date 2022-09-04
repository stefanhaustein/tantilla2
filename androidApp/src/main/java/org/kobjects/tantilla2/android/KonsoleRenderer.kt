package org.kobjects.tantilla2.android

import android.widget.ImageButton
import androidx.compose.foundation.Image
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
                viewModel.fileName.value,

            )
            Box {
                org.kobjects.konsole.compose.RenderKonsole(konsole = viewModel.console.konsole as ComposeKonsole)
                if (viewModel.showDpad.value) {
                    Row(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.ArrowLeft, "Left",
                            Modifier.size(48.dp)
                        )
                        Icon(Icons.Default.ArrowRight, "Right")
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.SmartButton, "Fire")
                    }
                }
            }
        }
    }
}
