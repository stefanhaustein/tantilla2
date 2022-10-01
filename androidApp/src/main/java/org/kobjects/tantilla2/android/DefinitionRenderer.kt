package org.kobjects.tantilla2.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.kobjects.konsole.compose.AnsiConverter
import org.kobjects.tantilla2.android.model.TantillaViewModel
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.Palette
import org.kobjects.tantilla2.core.Scope

@Composable
fun RenderDefinition(viewModel: TantillaViewModel, definition: Definition) {
    val expanded = viewModel.expanded.value.contains(definition)
    Card(
        backgroundColor = if (!expanded && (viewModel.userRootScope.definitionsWithErrors.contains(definition)
                    || definition.errors.isNotEmpty()
                    || definition == viewModel.runtimeException.value?.definition)) Color(Palette.BRIGHTEST_RED) else Color(0xffeeeeee),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .padding(4.dp)
            .pointerInput(Unit) {
                val editable =
                    !definition.isScope() && viewModel.mode.value == TantillaViewModel.Mode.HIERARCHY
                detectTapGestures(
                    onLongPress = {
                        if (definition.isScope()) {
                            viewModel.scope().value = definition.getValue(null) as Scope
                        } else if (editable) {
                            viewModel.edit(definition)
                        }
                    },
                    onTap = {
                        if (viewModel.expanded.value.contains(definition)) {
                            viewModel.expanded.value -= definition
                        } else {
                            viewModel.expanded.value += definition
                        }
                    }
                )
            }
    ) {
        Box(Modifier.padding(8.dp)) {
        //    val expanded = viewModel.expanded.value.contains(definition)
            val help = viewModel.mode.value == TantillaViewModel.Mode.HELP
            Column() {
                val writer = CodeWriter(highlighting = CodeWriter.defaultHighlighting)
                if (!expanded) {
                    definition.serializeTitle(writer)
                } else {
                    definition.serializeSummary(writer)
                }
                Text(AnsiConverter.ansiToAnnotatedString(writer.toString()))
            }
            Row(modifier = Modifier
                .align(Alignment.TopEnd)
                .alpha(0.2f)) {
                if (definition.isScope() || !help) {
                    Icon(
                        Icons.Default.Fullscreen,
                        contentDescription = "Open",
                        modifier = Modifier.clickable {
                            if (definition.isScope()) {
                                viewModel.scope().value = definition.getValue(null) as Scope
                            } else {
                                viewModel.edit(definition)
                            }
                        })
                }
            }
        }
    }
}
