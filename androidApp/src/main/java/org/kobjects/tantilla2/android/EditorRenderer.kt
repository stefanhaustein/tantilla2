package org.kobjects.tantilla2.android

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.kobjects.greenspun.core.F64
import org.kobjects.konsole.compose.AnsiConverter.ansiToAnnotatedString
import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.android.model.TantillaViewModel
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.highlightSyntax


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RenderEditor(viewModel: TantillaViewModel) {
    var showMenu = remember { mutableStateOf(false) }
    val scope = viewModel.userScope.value
    val definition = viewModel.definition.value
    val errors = remember {
        mutableStateOf(viewModel.definition.value?.errors ?: emptyList())
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
        },
        bottomBar = {
            Divider()
            val errors = errors.value
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
                        annotatedString = TantillaViewModel.annotatedCode(it.text, errors.value))
                },
                Modifier.fillMaxSize(),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent
                )

            )
    }
}
