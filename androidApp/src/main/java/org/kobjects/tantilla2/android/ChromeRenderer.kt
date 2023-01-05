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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.kobjects.tantilla2.android.model.TantillaViewModel
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.UserRootScope
import org.kobjects.tantilla2.core.definition.SystemRootScope

@Composable
fun RenderAppBar(
    viewModel: TantillaViewModel,
    title: String,
) {

    val showMenu = remember { mutableStateOf(false) }
    val showAddMenu = remember { mutableStateOf(false) }
    val showExamplesMenu = remember { mutableStateOf(false) }
    val showClearMenu = remember { mutableStateOf(false) }
    val showFileMenu = remember { mutableStateOf(false) }
    val showLoadMenu = remember { mutableStateOf(false) }


        TopAppBar(
        /*    backgroundColor =key (viewModel.codeUpdateTrigger.value) {
            if (viewModel.userRootScope.definitionsWithErrors.isEmpty()) Color(Palette.BLUE) else Color(
                Palette.ORANGE
            )}, */
            title = {
                val scope = viewModel.scope().value
                if (viewModel.mode.value == TantillaViewModel.Mode.SHELL) {
                    Text(viewModel.fileName.value, maxLines = 1, overflow = TextOverflow.Ellipsis)
                } else if (scope is UserRootScope || scope is SystemRootScope){
                    Text(viewModel.definitionTitle(scope), maxLines = 1, overflow = TextOverflow.Ellipsis)
                } else {
                    Column(Modifier.clickable {
                        val target = viewModel.scope().value.parentScope ?: viewModel.systemRootScope
                        val stackSize = viewModel.navigationStack.size
                        viewModel.navigateBack(target)
                    }) {
                        Text("❮ " + viewModel.definitionTitle(scope.parentScope), fontSize = 10.sp, fontFamily = FontFamily.SansSerif)
                        Text(viewModel.definitionTitle(scope), maxLines = 1, fontSize = 18.sp, overflow = TextOverflow.Ellipsis)
                    }
                }
            },
            actions = {
                for (mode in listOf(
                    TantillaViewModel.Mode.HELP,
                    TantillaViewModel.Mode.HIERARCHY,
                    TantillaViewModel.Mode.SHELL
                )) {
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
                                    .padding(0.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { when (mode) {
                                TantillaViewModel.Mode.HELP -> viewModel.navigateTo(viewModel.currentHelpScope.value)
                                TantillaViewModel.Mode.HIERARCHY -> viewModel.navigateTo(viewModel.currentUserScope.value)
                                TantillaViewModel.Mode.SHELL -> viewModel.navigateTo(null)
                            } }
                        ) {
                            Icon(
                                icon,
                                //               tint = Color.White,
                                contentDescription = mode.toString()
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { showMenu.value = !showMenu.value }
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                    var menuItems = buildList {
                        key(viewModel.runstateUpdateTrigger.value) {
                            if (viewModel.globalRuntimeContext.activeThreads == 0)
                                add("Run main()" to { viewModel.runMain() })
                            else add("Stop" to { viewModel.stop() })
                        }
                        when (viewModel.mode.value) {
                            TantillaViewModel.Mode.HIERARCHY -> {
                                add("Add  ▶" to { showAddMenu.value = true })
                                val scope = viewModel.currentUserScope.value
                                val parent = scope.parentScope
                                if (parent !is SystemRootScope && parent != null) {
                                    add("Delete" to {
                                        viewModel.dialogManager.showConfirmation(
                                            "Confirm",
                                            "Delete '$scope'?"
                                        ) {
                                            parent.remove(scope.toString())
                                            viewModel.currentUserScope.value = parent
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
                        "Field" to { viewModel.addDefinition(Definition.Kind.PROPERTY) },
                        "Function" to { viewModel.addDefinition(Definition.Kind.FUNCTION) },
                        "" to { },
                        "Struct" to { viewModel.addDefinition(Definition.Kind.STRUCT) },
                        "Trait" to { viewModel.addDefinition(Definition.Kind.TRAIT) },
                        "Impl" to { viewModel.addDefinition(Definition.Kind.IMPL) },
                        "Unit" to { viewModel.addDefinition(Definition.Kind.UNIT) }
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
                        "FizzBuzz" to { viewModel.loadExample("FizzBuzz.tt") },
                        "GraphicsDemo" to { viewModel.loadExample("GraphicsDemo.tt") },
                        "RayTracer" to { viewModel.loadExample("RayTracer.tt") },
                        "RuntimeError" to { viewModel.loadExample("RuntimeError.tt") },
                        "CompilationError" to { viewModel.loadExample("CompilationError.tt") },
                        "Snake" to { viewModel.loadExample("Snake.tt") },
                        "AoC" to { viewModel.loadExample("AoC.tt") },
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

