package org.kobjects.dialog

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

class DialogManager {
    val modalDialog: MutableState<DialogSpecification?> = mutableStateOf(null)


    fun showPrompt(
        title: String,
        text: String,
        input: String = "",
        confirmLabel: String = "Ok",
        dismissLabel: String? = "Cancel",
        onConfirm: (String) -> Unit
    ) {
        modalDialog.value = DialogSpecification(
            title = title,
            lines = listOf(InputLine(text, input)),
            dismissLabel = dismissLabel,
            confirmLabel = confirmLabel,
            onConfirm = { onConfirm(it[0] as String) }
        )
    }

    fun showAlert(
        title: String,
        text: String,
        confirmLabel: String = "Ok",
        onConfirm: () -> Unit = {}
    ) {
        showConfirmation(title, text, confirmLabel, dismissLabel = null, onConfirm)
    }

    fun showConfirmation(
        title: String,
        text: String,
        confirmLabel: String = "Ok",
        dismissLabel: String? = "Cancel",
        onConfirm: () -> Unit = {}
    ) {
        modalDialog.value = DialogSpecification(
            title = title,
            text = text,
            dismissLabel = dismissLabel,
            confirmLabel = confirmLabel,
            onConfirm = { onConfirm() }
        )
    }

    fun showError(message: String) {
        showAlert("Error", message)
    }

    fun showCustom(
        title: String,
        text: String = "",
        inputLines: List<InputLine<*>> = listOf(),
        confirmLabel: String = "Ok",
        dismissLabel: String = "Cancel",
        onConfirm: (List<*>) -> Unit) {
        modalDialog.value = DialogSpecification(title, text, inputLines, confirmLabel, dismissLabel, onConfirm)
    }


}