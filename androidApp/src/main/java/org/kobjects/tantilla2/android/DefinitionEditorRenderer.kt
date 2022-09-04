package org.kobjects.tantilla2.android

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.kobjects.tantilla2.android.model.TantillaViewModel


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RenderDefinitionEditor(viewModel: TantillaViewModel) {
    var showMenu = remember { mutableStateOf(false) }
    val scope = viewModel.userScope.value
    val definition = viewModel.editingDefinition.value
    val errors = remember {
        mutableStateOf(viewModel.editingDefinition.value?.errors ?: emptyList())
    }
    val errorIndex = remember { mutableStateOf(0) }

    Scaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(text = definition?.name ?: "New Property") },
                actions = {
                    IconButton(onClick = {
                        scope.update(viewModel.currentText.value.text, definition)
                        println("Updating to : " + viewModel.currentText.value.text)
                        viewModel.mode.value = TantillaViewModel.Mode.HIERARCHY
                        viewModel.runtimeException.value = null
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
                            viewModel.mode.value = TantillaViewModel.Mode.HIERARCHY
                        }) {
                            Text("Cancel")
                        }
                        if (definition != null) {
                            DropdownMenuItem(onClick = {
                                scope.remove(definition.name)
                                viewModel.mode.value = TantillaViewModel.Mode.HIERARCHY
                            }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            Divider()
            val errors = if (viewModel.runtimeException.value?.definition != definition) errors.value else listOf(viewModel.runtimeException.value!!) + errors.value
            Row() {
                IconButton(
                    enabled = errors.size > 1,
                    onClick = { errorIndex.value-- }
                ) {
                    Icon(Icons.Default.ArrowLeft, "Previous")
                }
                if (errors.isEmpty()) {
                    Text("(No error)", Modifier.weight(1f)
                        .height(48.dp)
                        .padding(4.dp, 10.dp, 4.dp, 4.dp))
                } else {
                    val error = errors[errorIndex.value % errors.size]
                    val text = error.message ?: error.toString()
                    Text(text, Modifier.weight(1f)
                        .height(48.dp)
                        .padding(4.dp))
                }

                IconButton(
                    enabled = errors.size > 1,
                    onClick = { errorIndex.value++ }
                ) {
                    Icon(Icons.Default.ArrowRight, "Next")
                }
            }
        },
    ) {
            TextField(
                value = viewModel.currentText.value,
                onValueChange = {
                    if (it.text != viewModel.currentText.value.text) {
                        errors.value = scope.checkErrors(it.text)
                    }
                    viewModel.currentText.value = it.copy(
                        annotatedString = viewModel.annotatedCode(it.text, errors.value))
                },
                Modifier.fillMaxSize(),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent
                )

            )
    }
}
