package org.kobjects.tantilla2.android

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


@Composable
fun RenderEditor(viewModel: TantillaViewModel) {
    var showMenu = remember { mutableStateOf(false) }
    val scope = viewModel.userScope.value
    val definition = viewModel.definition.value
    Scaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(text = definition?.name ?: "New Property") },
                actions = {
                    IconButton(onClick = {
                        scope.update(viewModel.currentText.value.text, definition)
                        viewModel.editing.value = false
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                    IconButton(onClick = { showMenu.value = !showMenu.value }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showMenu.value,
                        onDismissRequest = { showMenu.value = false }
                    ) {
                        /* DropdownMenuItem(onClick = {  }) {
                        Text("Move")
                    } */
                        DropdownMenuItem(onClick = {
                            viewModel.editing.value = false
                        }) {
                            Text("Cancel")
                        }
                        if (definition != null) {
                            DropdownMenuItem(onClick = {
                                scope.definitions.remove(definition.name)
                                viewModel.editing.value = false
                            }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            )
        }
    ) {
        Column() {
            val error = viewModel.definition.value!!.error()
            if (error != null) {
                Text(error.message ?: error.toString())
            }
            TextField(
                value = viewModel.currentText.value,
                onValueChange = {
                    viewModel.currentText.value = it
                    /*
                    viewModel.definition.value = scope.update(viewModel.currentText.value.text, viewModel.definition.value)
                    if (viewModel.definition.value.toString() == viewModel.currentText.value.text) {
                        val writer = CodeWriter(highlighting = CodeWriter.defaultHighlighting)
                        definition?.serializeCode(writer)
                    viewModel.currentText.value = viewModel.currentText.value.copy(annotatedString = ansiToAnnotatedString(writer.toString())) //.withError(definition?.error()))
                    }
                    */
                },
                modifier = Modifier.fillMaxSize(),
            )

        }
    }
}
