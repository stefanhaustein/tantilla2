package org.kobjects.tantilla2.android

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.kobjects.konsole.compose.AnsiConverter.ansiToAnnotatedString
import org.kobjects.konsole.compose.ComposeKonsole
import org.kobjects.konsole.compose.RenderKonsole
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.UserScope
import org.kobjects.tantilla2.core.runtime.RootScope


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

