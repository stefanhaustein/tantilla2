package org.kobjects.tantilla2.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.kobjects.konsole.compose.AnsiConverter
import org.kobjects.tantilla2.android.model.TantillaViewModel
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.Palette
import org.kobjects.tantilla2.core.scope.AbsoluteRootScope

@Composable
fun RenderDefinition(viewModel: TantillaViewModel, definition: Definition, textWidth: MutableState<Int>) {
    val expanded = viewModel.expanded.value.contains(definition)

    Card(
        backgroundColor = if (!expanded
            && (viewModel.userRootScope.definitionsWithErrors.containsKey(definition)
                    || viewModel.userRootScope.definitionsWithChildErrors[definition]?.isNotEmpty() == true
                    || definition == viewModel.runtimeException.value?.definition)) Color(Palette.BRIGHTEST_RED) else Color(0xffeeeeee),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .padding(4.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        viewModel.navigateTo(definition)
                    }) }
    ) {
        Box(Modifier.padding(8.dp)) {
        //    val expanded = viewModel.expanded.value.contains(definition)
            val help = viewModel.mode.value == TantillaViewModel.Mode.HELP
            Column {
                val writer = CodeWriter(
                    highlighting = CodeWriter.defaultHighlighting,
                    errorNode = viewModel.runtimeException.value?.node,
                    lineLength = textWidth.value,
                    forTitle = !expanded,
                    scope = definition.parentScope ?: AbsoluteRootScope
                )
                // writer.append(Ansi.NOT_PROPORTIONAL)

                definition.serializeSummary(writer, if (expanded) Definition.SummaryKind.EXPANDED else Definition.SummaryKind.COLLAPSED)

                Text(AnsiConverter.ansiToAnnotatedString(writer.toString(),
                        TantillaViewModel.MONOSPACE_FONT_FAMILY,
                        TantillaViewModel.MONOSPACE_FONT_FAMILY),
                    overflow = TextOverflow.Clip,
                    softWrap = false,
                    onTextLayout = { updateTextWidth(it, textWidth) }

                )
            }
            Row(modifier = Modifier
                .align(Alignment.TopEnd)
                .alpha(0.2f)) {
                if (definition.isSummaryExpandable()) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Open",
                        modifier = Modifier.clickable {
                                if (viewModel.expanded.value.contains(definition)) {
                                    viewModel.expanded.value -= definition
                                } else {
                                    viewModel.expanded.value += definition
                                }
                        })
                }
            }
        }
    }
}
