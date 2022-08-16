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
import org.kobjects.tantilla2.android.model.TantillaViewModel


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
                        println("Updating to : " + viewModel.currentText.value.text)
                        viewModel.editing.value = false
                        viewModel.save()
                        viewModel.forceUpdate.value++
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
                                scope.remove(definition.name)
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
            val errors = viewModel.definition.value?.errors ?: emptyList()
            if (!errors.isEmpty()) {
                Text(errors[0].message ?: errors[0].toString())
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
