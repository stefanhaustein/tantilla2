package org.kobjects.dialog.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.kobjects.dialog.DialogManager
import org.kobjects.dialog.DialogSpecification


@Composable
fun RenderDialogs(manager: DialogManager) {
    val modalDialog = manager.modalDialog.value
    if (modalDialog != null) {
        RenderDialog(manager, modalDialog)
    }
}

@Composable
fun RenderDialog(manager: DialogManager, dialog: DialogSpecification) {
    val inputState = remember { MutableList< MutableState<in Any>>(dialog.lines.size) {
        mutableStateOf(dialog.lines[it].value)}}
    AlertDialog(
        onDismissRequest = { manager.modalDialog.value = null },
        title = { Text(text = dialog.title) },
        text = {
            Column() {
                if (dialog.text.isNotBlank()) {
                    Text(text = dialog.text)
                }
                for (index in dialog.lines.indices) {
                    val state = inputState[index]
                    val line = dialog.lines[index]
                    if (line.label.isNotBlank()) {
                        Text(text = line.label.replace("#", state.value.toString()))
                    }
                    if (line.range != null) {
                        val intermediate = remember { mutableStateOf(inputState[index].value as Float) }
                        Slider(value = intermediate.value,
                            steps =  line.steps,
                            valueRange = line.range,
                            onValueChange = { intermediate.value = it },
                            onValueChangeFinished = { state.value = intermediate.value })
                    } else if (line.options != null || line.value is Boolean) {
                        val options = line.options ?: mapOf(true to "true", false to "false")
                        val expanded = remember { mutableStateOf(false) }
                        Button(onClick = { expanded.value = true }) {
                            Text(options[state.value].toString())
                            DropdownMenu(expanded.value, onDismissRequest = { expanded.value = false }) {
                                for (entry in options) {
                                    DropdownMenuItem(
                                        onClick = {
                                            expanded.value = false
                                            state.value = entry.key!!
                                        }) {
                                            Text(entry.value)
                                        }
                                }
                            }
                        }
                    } else {
                        TextField(
                            value = inputState[index].value.toString(),
                            onValueChange = { inputState[index].value = it })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    manager.modalDialog.value = null
                    dialog.onConfirm(List(inputState.size) { inputState[it].value })
                }
            ) {
                Text(text = dialog.confirmLabel)
            }
        },
        dismissButton = if (dialog.dismissLabel == null) null else ({
            Button(onClick = { manager.modalDialog.value = null }) {
                Text(text = dialog.dismissLabel)
            }
        })
    )
}