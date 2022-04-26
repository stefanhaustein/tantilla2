package org.kobjects.tantilla2.android

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import org.kobjects.konsole.compose.RenderKonsole
import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.Scope


@Composable
fun Render(viewModel: TantillaViewModel) {

        Column() {
            when (viewModel.mode.value) {
                TantillaViewModel.Mode.BUILTIN_SCOPE,
                TantillaViewModel.Mode.USER_SCOPE -> RenderScope(viewModel)
                TantillaViewModel.Mode.EDIT -> RenderEditor(viewModel)
                TantillaViewModel.Mode.INTERACTIVE ->
                    RenderKonsole(viewModel)
            }
        }
    }


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
            TopAppBar(
                title = { Text(text = "Tantilla 2") },
                actions = {
                    IconButton(onClick = {
                        viewModel.mode.value = TantillaViewModel.Mode.BUILTIN_SCOPE
                    }) {
                        Icon(Icons.Filled.Help, contentDescription = "Help")
                    }
                    IconButton(onClick = {
                        viewModel.mode.value = TantillaViewModel.Mode.USER_SCOPE
                    }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                }
            )
            RenderKonsole(konsole = viewModel.konsole)
        }
    }
}

@Composable
fun RenderScope(viewModel: TantillaViewModel) {
    val scopeDetails = viewModel.scopeDetails()
    val scope = scopeDetails.scope.value

    Column() {
        TopAppBar(
            title = { Text(text = scopeDetails.title.value) },
            actions = {
                IconButton(onClick = { viewModel.mode.value = TantillaViewModel.Mode.INTERACTIVE }) {
                    Icon(Icons.Filled.Close, contentDescription = "Back")
                }
            }
        )
    }

    val builtin = viewModel.mode.value == TantillaViewModel.Mode.BUILTIN_SCOPE
    val definitions = scope.definitions.values.sortedBy { it.name }.filter { it.builtin == builtin }

    LazyColumn(
        Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {

        itemsIndexed(definitions, { index, def -> def.name }) { index, def ->
                RenderDefinition(viewModel, def)
        }
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RenderDefinition(viewModel: TantillaViewModel, definition: Definition) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .padding(4.dp),
        onClick = {
            if (viewModel.mode.value== TantillaViewModel.Mode.USER_SCOPE) {
                viewModel.edit(definition.name)
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(definition.name)
        }
    }
}

@Composable
fun RenderEditor(viewModel: TantillaViewModel) {
    var showMenu = remember { mutableStateOf(false) }
    Column() {
        TopAppBar(
            title = { Text(text = viewModel.detail.value) },
            actions = {
                IconButton(onClick = { viewModel.mode.value = TantillaViewModel.Mode.USER_SCOPE }) {
                    Icon(Icons.Default.Check, contentDescription = "Save")
                }
                IconButton(onClick = { showMenu.value = !showMenu.value }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = showMenu.value,
                    onDismissRequest = { showMenu.value = false }
                ) {
                    DropdownMenuItem(onClick = {  }) {
                        Text("Rename / Move")
                    }
                    DropdownMenuItem(onClick = {  }) {
                        Text("Change Signature")
                    }
                    DropdownMenuItem(onClick = {  }) {
                        Text("Delete")
                    }
                    DropdownMenuItem(onClick = { viewModel.mode.value = TantillaViewModel.Mode.USER_SCOPE }) {
                        Text("Cancel")
                    }
                }
            }
        )
        TextField(
            value = viewModel.currentText.value,
            onValueChange = { viewModel.currentText.value = it },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
