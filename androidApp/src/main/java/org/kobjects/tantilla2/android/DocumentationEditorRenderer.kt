package org.kobjects.tantilla2.android

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
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


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RenderDocumentationEditor(viewModel: TantillaViewModel) {
    var showMenu = remember { mutableStateOf(false) }
    val scope = viewModel.userScope.value

    Scaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(text = scope.name ) },
                actions = {
                    IconButton(onClick = {
                        scope.docString = viewModel.currentText.value.text
                        viewModel.mode.value = TantillaViewModel.Mode.HIERARCHY
                        viewModel.notifyCodeChangedAndSave()
                        viewModel.codeUpdateTrigger.value++
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

                    }
                }
            )
        },

    ) {
            TextField(
                value = viewModel.currentText.value,
                onValueChange = {
                    viewModel.currentText.value = it
                },
                Modifier.fillMaxSize(),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent
                )
            )
    }
}
