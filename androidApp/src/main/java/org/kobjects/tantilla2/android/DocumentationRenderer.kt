package org.kobjects.tantilla2.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.kobjects.konsole.compose.AnsiConverter
import org.kobjects.tantilla2.android.model.TantillaViewModel
import org.kobjects.tantilla2.core.CodeWriter


@Composable
fun RenderDocumentation(viewModel: TantillaViewModel) {
    val expanded = remember {
        mutableStateOf(false)
    }
    val editable = viewModel.mode.value == TantillaViewModel.Mode.HIERARCHY
    val scope = viewModel.scope().value

    Box(
        Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {

                detectTapGestures(
                    onLongPress = {
                        if (editable) {
                            viewModel.editDocumentation()
                        }
                    },
                    onTap = {
                        expanded.value = !expanded.value
                    }
                )
            }) {

        Column() {
            if (scope.docString.isBlank()) {
                Text("(Undocumented)", color = Color.LightGray)
            } else {
                val writer = CodeWriter(
                    highlighting = CodeWriter.defaultHighlighting,
                    lineLength = 40
                )
                if (expanded.value) {
                    writer.appendWrapped(CodeWriter.Kind.STRING, scope.docString)
                } else {
                    writer.appendWrapped(
                        CodeWriter.Kind.STRING,
                        scope.docString.split("\n").first()
                    )
                }
                Text(
                    AnsiConverter.ansiToAnnotatedString(writer.toString()),
                    Modifier.padding(bottom = 6.dp))
            }
        }
        if (editable) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .alpha(0.2f)
                    .padding(end = 12.dp)
            ) {

                Icon(
                    Icons.Default.Fullscreen,
                    contentDescription = "Open",
                    modifier = Modifier.clickable {
                        viewModel.editDocumentation()
                    })

            }
        }
    }
}

