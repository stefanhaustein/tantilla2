package org.kobjects.tantilla2.android

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.kobjects.tantilla2.android.model.TantillaViewModel


@Composable
fun RenderDefinitionEditor(viewModel: TantillaViewModel) {
    var showMenu = remember { mutableStateOf(false) }
    val scope = viewModel.currentUserScope.value
    val definition = viewModel.editingDefinition.value
    val errors = remember {
        mutableStateOf(viewModel.editingDefinition.value?.errors ?: emptyList())
    }
    val textWidth = remember { mutableStateOf(40) }
    val errorIndex = remember { mutableStateOf(0) }

    Scaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column() {
                        Text(viewModel.definitionTitle(scope), fontSize = 10.sp, fontFamily = FontFamily.SansSerif)
                        Text(text = definition?.name ?: "New Property")
                    }},
                actions = {
                    IconButton(onClick = {
                        viewModel.closeEditorAndSave()
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
            val errors = if (viewModel.runtimeException.value?.definition != definition) errors.value
                else listOf(viewModel.runtimeException.value!!) + errors.value
            Row() {
                IconButton(
                    enabled = errors.size > 1,
                    onClick = { errorIndex.value-- }
                ) {
                    Icon(Icons.Default.ArrowLeft, "Previous")
                }
                if (errors.isEmpty()) {
                    Text("(No error)",
                        Modifier
                            .weight(1f)
                            .height(48.dp)
                            .padding(4.dp, 10.dp, 4.dp, 4.dp)

                    )
                } else {
                    val error = errors[errorIndex.value % errors.size]
                    val text = error.message ?: error.toString()
                    Text(text,
                        Modifier
                            .weight(1f)
                            .height(48.dp)
                            .padding(4.dp)
                            .clickable {
                                viewModel.dialogManager.showError(text)
                            })
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

        val textPx = with (LocalDensity.current) {
            LocalTextStyle.current.fontSize.toPx()
        }
        val padding = with(LocalDensity.current) {
            20.dp.toPx()
        }
        println("DefinitionEditor composition")

            TextField(
                value = viewModel.currentText.value,
                onValueChange = {
                    if (it.text != viewModel.currentText.value.text) {
                        errors.value = scope.checkErrors(it.text)
                    }
                    viewModel.currentText.value = it.copy(
                        annotatedString = viewModel.annotatedCode(it.text, errors.value),
                    )
                },
                Modifier
                    .fillMaxSize()
                    .padding(it)
                    //    .background(Color.Yellow)
                    .onSizeChanged {
                        val availableWidth = it.width - 2 * padding
                        viewModel.setEditorLineLength(
                            (availableWidth / (textPx / 2f)).toInt() - 2
                        )
                        println("Editor text width: ${textWidth.value}")
                    },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent
                ),

                //onTextLayout = { updateTextWidth(it, textWidth) }
            )
    }
}
