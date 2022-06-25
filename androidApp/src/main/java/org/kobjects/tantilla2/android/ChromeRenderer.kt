package org.kobjects.tantilla2.android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.kobjects.konsole.compose.ComposeKonsole
import org.kobjects.tantilla2.core.UserScope
import org.kobjects.tantilla2.core.runtime.RootScope

@Composable
fun RenderAppBar(
    viewModel: TantillaViewModel,
    title: String,
    vararg extraMenuItems: Pair<String, () -> Unit>) {

    val showMenu = remember { mutableStateOf(false) }
    val showExamplesMenu = remember { mutableStateOf(false) }
    val showClearMenu = remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            if (viewModel.mode.value != TantillaViewModel.Mode.SHELL
                && viewModel.scope().value !is RootScope && viewModel.scope().value !is UserScope
            ) {
                Text(
                    text = "❮ $title",
                    Modifier.clickable {
                        viewModel.scope().value = viewModel.scope().value.parentContext!!
                    })
            } else {
                Text(text = title)
            }
        },
        actions = {
            for (mode in TantillaViewModel.Mode.values()) {
                val icon = when (mode) {
                    TantillaViewModel.Mode.HELP -> Icons.Default.Help
                    TantillaViewModel.Mode.HIERARCHY -> Icons.Default.ViewList // Article
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
                    "Run main()" to { viewModel.runMain() },
                    "Clear ▶" to { showClearMenu.value = true },
                    "Examples \u25B6" to { showExamplesMenu.value = true },
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
                RenderDropDownMenu(
                    showClearMenu,
                    "Clear text output" to { (viewModel.console.konsole as ComposeKonsole).entries.clear() },
                            "Full reset" to { viewModel.reset() }
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