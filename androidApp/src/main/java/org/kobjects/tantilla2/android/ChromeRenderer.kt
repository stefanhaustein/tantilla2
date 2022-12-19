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
import androidx.compose.ui.unit.dp
import org.kobjects.tantilla2.android.model.TantillaViewModel
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.UserRootScope
import org.kobjects.tantilla2.core.definition.RootScope

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
        /*    backgroundColor =key (viewModel.codeUpdateTrigger.value) {
            if (viewModel.userRootScope.definitionsWithErrors.isEmpty()) Color(Palette.BLUE) else Color(
                Palette.ORANGE
            )}, */
            title = {
                if (viewModel.mode.value != TantillaViewModel.Mode.SHELL
                    && viewModel.scope().value !is RootScope && viewModel.scope().value !is UserRootScope
                ) {
                    Text(
                        text = "❮ $title",
                        Modifier.clickable {
                            viewModel.scope().value = viewModel.scope().value.parentScope ?: viewModel.rootScope
                        })
                } else {
                    Text(text = title)
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
                            onClick = { viewModel.mode.value = mode }
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
                                if (parent !is RootScope && parent != null) {
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
                        "AoC Day 1a" to { viewModel.loadExample("aoc2022/Day01a.tt") },
                        "AoC Day 1b" to { viewModel.loadExample("aoc2022/Day01b.tt") },
                        "AoC Day 2a" to { viewModel.loadExample("aoc2022/Day02a.tt") },
                        "AoC Day 2b" to { viewModel.loadExample("aoc2022/Day02b.tt") },
                        "AoC Day 3a" to { viewModel.loadExample("aoc2022/Day03a.tt") },
                        "AoC Day 3b" to { viewModel.loadExample("aoc2022/Day03b.tt") },
                        "AoC Day 4a" to { viewModel.loadExample("aoc2022/Day04a.tt") },
                        "AoC Day 4b" to { viewModel.loadExample("aoc2022/Day04b.tt") },
                        "AoC Day 5a" to { viewModel.loadExample("aoc2022/Day05a.tt") },
                        "AoC Day 5b" to { viewModel.loadExample("aoc2022/Day05b.tt") },
                        "AoC Day 6a" to { viewModel.loadExample("aoc2022/Day06a.tt") },
                        "AoC Day 6b" to { viewModel.loadExample("aoc2022/Day06b.tt") },
                        "AoC Day 7a" to { viewModel.loadExample("aoc2022/Day07a.tt") },
                        "AoC Day 7b" to { viewModel.loadExample("aoc2022/Day07b.tt") },
                        "AoC Day 8a" to { viewModel.loadExample("aoc2022/Day08a.tt") },
                        "AoC Day 8b" to { viewModel.loadExample("aoc2022/Day08b.tt") },
                        "AoC Day 9a" to { viewModel.loadExample("aoc2022/Day09a.tt") },
                        "AoC Day 9b" to { viewModel.loadExample("aoc2022/Day09b.tt") },
                        "AoC Day 10a" to { viewModel.loadExample("aoc2022/Day10a.tt") },
                        "AoC Day 10b" to { viewModel.loadExample("aoc2022/Day10b.tt") },
                        "AoC Day 11a" to { viewModel.loadExample("aoc2022/Day11a.tt") },
                        "AoC Day 11b" to { viewModel.loadExample("aoc2022/Day11b.tt") },
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

