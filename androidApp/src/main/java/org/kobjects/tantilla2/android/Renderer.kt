package org.kobjects.tantilla2.android

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable


@Composable
fun Render(viewModel: TantillaViewModel) {

        Column() {
            if (viewModel.editing.value) {
                RenderEditor(viewModel = viewModel)
            } else {
                when (viewModel.mode.value) {
                    TantillaViewModel.Mode.HELP,
                    TantillaViewModel.Mode.HIERARCHY -> RenderScope(viewModel)
                    TantillaViewModel.Mode.SHELL -> RenderKonsole(viewModel)
                }
            }
        }
    }

