package org.kobjects.tantilla2.android

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.unit.dp
import org.kobjects.konsole.compose.ComposeKonsole
import org.kobjects.konsole.compose.RenderKonsole
import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.Scope


@Composable
fun Render(viewModel: TantillaViewModel) {

        Column() {
            if (viewModel.editing.value) {
                RenderEditor(viewModel = viewModel)
            } else {
                when (viewModel.mode.value) {
                    TantillaViewModel.Mode.HELP,
                    TantillaViewModel.Mode.HIERARCHY -> RenderScope(viewModel)
                    TantillaViewModel.Mode.SHELL ->
                        RenderKonsole(viewModel)
                }
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
            RenderAppBar(
                viewModel,
                if (viewModel.fileName.value.isEmpty()) "Tantilla 2" else  viewModel.fileName.value)

            RenderKonsole(konsole = viewModel.console.konsole as ComposeKonsole)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RenderScope(viewModel: TantillaViewModel) {
    val scope = viewModel.scope().value

    val builtin = viewModel.mode.value == TantillaViewModel.Mode.HELP
    val definitions = scope.definitions.values.sortedBy { it.name }.filter { it.builtin == builtin }

    Scaffold(
        backgroundColor = Color.Transparent,
        bottomBar = {
            if (scope.parentContext != null) {
                BottomAppBar(
                    Modifier.clickable {
                        viewModel.scope().value = scope.parentContext!!
                    }

                ) {
                    IconButton(onClick ={viewModel.scope().value = scope.parentContext!!} ) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                    Text(scope.parentContext!!.title)
                }
            }
        },
        topBar = {
            if (viewModel.mode.value == TantillaViewModel.Mode.HELP) {
                RenderAppBar(viewModel, if (scope.parentContext == null) "Help" else scope.title)
            } else {
                RenderAppBar(viewModel, scope.title, "Add" to {viewModel.edit(scope, null)})
            } },
    ) { LazyColumn(
        Modifier
            .fillMaxWidth()
            .padding(4.dp)
        ) {
            itemsIndexed(definitions, { index, def -> def.name }) { index, def ->
                RenderDefinition(viewModel, def)
            }
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
            if (viewModel.mode.value == TantillaViewModel.Mode.HIERARCHY) {
                if (definition.value() is Scope) {
                    viewModel.userScope.value = definition.value() as Scope
                } else {
                    viewModel.edit(definition.scope, definition)
                }
            }
        }
    ) {
      /*  Column(
            modifier = Modifier.padding(8.dp)
        ) {*/
            Text(

                definition.title(), Modifier.padding(8.dp))
       // }
    }
}

@Composable
fun RenderEditor(viewModel: TantillaViewModel) {
    var showMenu = remember { mutableStateOf(false) }
    val scope = viewModel.userScope.value
    val definition = viewModel.definition.value
    Scaffold(
        backgroundColor = Color.Transparent,
        bottomBar = { BottomAppBar(
            Modifier.clickable {
                scope.update(viewModel.currentText.value, definition)
                viewModel.editing.value = false
            }
        ) {
            IconButton(onClick = {
                scope.update(viewModel.currentText.value, definition)
                viewModel.editing.value = false
            }) {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
            Text("Save in ${viewModel.editorParentScope.title}")
        }},
        topBar = {
            TopAppBar(
                title = { Text(text = definition?.name ?: "New Property") },
                actions = {

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
                                scope.definitions.remove(definition!!.name)
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
        TextField(
            value = viewModel.currentText.value,
            onValueChange = { viewModel.currentText.value = it },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun RenderAppBar(
    viewModel: TantillaViewModel,
    title: String,
    vararg extraMenuItems: Pair<String, () -> Unit>) {

    var showMenu = remember { mutableStateOf(false) }
    var showExamplesMenu = remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = title) },
        actions = {
            for (mode in TantillaViewModel.Mode.values()) {
                val icon = when (mode) {
                    TantillaViewModel.Mode.HELP -> Icons.Default.Help
                    TantillaViewModel.Mode.HIERARCHY -> Icons.Default.ListAlt
                    TantillaViewModel.Mode.SHELL -> Icons.Default.Forum
                }
                if (mode == viewModel.mode.value) {
                    Column() {
                        Icon(
                            icon,
                            contentDescription = mode.toString(),
                            tint = Color.White,
                            modifier = Modifier.padding(12.dp, 12.dp, 12.dp, 8.dp)
                        )
                        Divider(
                            Modifier
                                .background(Color.White)
                                .width(48.dp)
                                .height(4.dp)
                                .padding(0.dp))
                    }
                } else {
                    IconButton(
                        onClick = { viewModel.mode.value = mode }
                    ) {
                        Icon(icon, contentDescription = mode.toString())
                    }
                }
            }

            IconButton(
                onClick = { showMenu.value = !showMenu.value }
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
                var menuItems = arrayOf(
                    "Full Reset" to { viewModel.reset()},
                    "Examples \u25B6" to { showExamplesMenu.value = true }
                )

                if (!extraMenuItems.isEmpty()) {
                    menuItems = arrayOf(*extraMenuItems, "" to {}, *menuItems)
                }

                RenderDropDownMenu(showMenu, *menuItems)
                RenderDropDownMenu(
                    showExamplesMenu,
                    "HelloWorld" to  { viewModel.loadExample("HelloWorld.tt") },
                    "RayTracer" to  { viewModel.loadExample("RayTracer.tt") }
                )
            }
        }
    )
}

@Composable
fun RenderDropDownMenu(expanded: MutableState<Boolean>, vararg items: Pair<String, () -> Unit>) {
    DropdownMenu(
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false }
    ) {
        for (item in items) {
            if (item.first.isEmpty()) {
                Divider()
             } else {
                DropdownMenuItem(onClick = {
                    expanded.value = false
                    item.second()
                }) {
                    Text(item.first)
                }
             }
        }
    }

}