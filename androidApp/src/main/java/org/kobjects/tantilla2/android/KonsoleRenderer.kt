package org.kobjects.tantilla2.android

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import org.kobjects.konsole.compose.ComposeKonsole


@Composable
fun RenderKonsole(viewModel: TantillaViewModel) {
    Box() {
        Image(
            bitmap = viewModel.bitmap.asImageBitmap(),
            contentDescription = "Canvas",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )
        Column() {
            RenderAppBar(
                viewModel,
                if (viewModel.fileName.value.isEmpty()) "Tantilla 2" else  viewModel.fileName.value,

            )

            org.kobjects.konsole.compose.RenderKonsole(konsole = viewModel.console.konsole as ComposeKonsole)
        }
    }
}
