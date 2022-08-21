package org.kobjects.tantilla2.android

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import org.kobjects.dialog.compose.RenderDialogs
import org.kobjects.tantilla2.android.model.TantillaViewModel


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

