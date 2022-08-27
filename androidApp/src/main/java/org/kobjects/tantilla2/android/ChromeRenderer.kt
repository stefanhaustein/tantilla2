package org.kobjects.tantilla2.android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.kobjects.tantilla2.android.model.TantillaViewModel
import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.UserRootScope
import org.kobjects.tantilla2.core.runtime.RootScope

@Composable
fun RenderAppBar(
    viewModel: TantillaViewModel,
    title: String) {

    val showMenu = remember { mutableStateOf(false) }
    val showAddMenu = remember { mutableStateOf(false) }
    val showExamplesMenu = remember { mutableStateOf(false) }
    val showClearMenu = remember { mutableStateOf(false) }
    val showFileMenu = remember { mutableStateOf(false) }
    val showLoadMenu = remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            if (viewModel.mode.value != TantillaViewModel.Mode.SHELL
                && viewModel.scope().value !is RootScope && viewModel.scope().value !is UserRootScope
            ) {
                Text(
                    text = "❮ $title",
                    Modifier.clickable {
                        viewModel.scope().value = viewModel.scope().value.parentScope!!
                    })
            } else {
                Text(text = title)
            }
        },
        actions = {
            for (mode in listOf(TantillaViewModel.Mode.HELP, TantillaViewModel.Mode.HIERARCHY, TantillaViewModel.Mode.SHELL)) {
                val icon = when (mode) {
                    TantillaViewModel.Mode.HELP -> Icons.Default.Help
                    TantillaViewModel.Mode.HIERARCHY -> Icons.Default.ViewList // Article
                    TantillaViewModel.Mode.SHELL -> Icons.Default.Forum
                    else -> Icons.Default.Cancel // Other modes
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
                var menuItems = buildList {
                    if (viewModel.globalRuntimeContext.value.activeThreads == 0)
                        add("Run main()" to { viewModel.runMain() })
                    else add("Stop" to { viewModel.stop() })
                    when (viewModel.mode.value) {
                        TantillaViewModel.Mode.HIERARCHY -> {
                            add("Add  ▶" to { showAddMenu.value = true })
                            val scope = viewModel.userScope.value
                            val parent = scope.parentScope
                            if (parent != RootScope && parent != null) {
                                add("Delete" to {
                                    viewModel.dialogManager.showConfirmation(
                                        "Confirm",
                                        "Delete '$scope'?"
                                    ) {
                                        parent.remove(scope.toString())
                                        viewModel.userScope.value = parent
                                    }
                                })
                            }
                        }
                        TantillaViewModel.Mode.SHELL -> {
                            add("Clear ▶" to { showClearMenu.value = true })
                        }
                    }
                    add("File \u25B6" to { showFileMenu.value = true })
                }

                RenderDropDownMenu(showMenu, *menuItems.toTypedArray())
                RenderDropDownMenu(
                    showAddMenu,
                    "Field" to { viewModel.add(Definition.Kind.PROPERTY) },
                    "Function" to { viewModel.add(Definition.Kind.FUNCTION) },
                    "" to { },
                    "Struct" to { viewModel.add(Definition.Kind.STRUCT) },
                    "Trait" to { viewModel.add(Definition.Kind.TRAIT) },
                    "Impl" to { viewModel.add(Definition.Kind.IMPL) },
                    "Unit" to { viewModel.add(Definition.Kind.UNIT) }
                )

                RenderDropDownMenu(
                    showFileMenu,
                    "Save as..." to { viewModel.saveAs() },
                    "Load..." to { showLoadMenu.value = true },
                    "Examples \u25B6" to { showExamplesMenu.value = true },
                    "Full reset" to { viewModel.confirmReset() }
                    )
                RenderDropDownMenu(
                    showExamplesMenu,
                    "FizzBuzz" to  { viewModel.loadExample("FizzBuzz.tt") },
                    "RayTracer" to  { viewModel.loadExample("RayTracer.tt") }
                )
                RenderDropDownMenu(
                    showClearMenu,
                    "Clear text output" to { (viewModel.clearConsole()) },
                            "Clear bitmap" to { viewModel.clearBitmap() },

                )
                RenderFileSelector(viewModel, showLoadMenu)
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

@Composable
fun RenderFileSelector(viewModel: TantillaViewModel, expanded: MutableState<Boolean>) {
    DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
        for (file in viewModel.platform.rootDirectory.listFiles()) {
            DropdownMenuItem(onClick = {
                expanded.value = false
                viewModel.load(file)
            }) {
                Text(file.name)
            }
        }
    }
}

