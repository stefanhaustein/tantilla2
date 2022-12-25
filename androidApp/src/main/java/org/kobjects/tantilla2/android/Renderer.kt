package org.kobjects.tantilla2.android

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.TextLayoutResult
import org.kobjects.dialog.compose.RenderDialogs
import org.kobjects.tantilla2.android.model.TantillaViewModel


fun updateTextWidth(textLayoutResult: TextLayoutResult, textWidthState: MutableState<Int>) {
    val textPx = with(textLayoutResult.layoutInput.density) {
        textLayoutResult.layoutInput.style.fontSize.toPx()
    }
    textWidthState.value = (textLayoutResult.layoutInput.constraints.maxWidth / (textPx / 2f)).toInt() - 1
    println("TextWidth: ${textWidthState.value}")
}

@Composable
fun Render(viewModel: TantillaViewModel) {

        Column() {
                when (viewModel.mode.value) {
                    TantillaViewModel.Mode.DEFINITION_EDITOR -> RenderDefinitionEditor(viewModel)
                    TantillaViewModel.Mode.DOCUMENTATION_EDITOR -> RenderDocumentationEditor(viewModel)
                    TantillaViewModel.Mode.HELP,
                    TantillaViewModel.Mode.HIERARCHY -> RenderScope(viewModel)
                    TantillaViewModel.Mode.SHELL -> RenderKonsole(viewModel)
                }

            RenderDialogs(manager = viewModel.dialogManager)
        }
    }

