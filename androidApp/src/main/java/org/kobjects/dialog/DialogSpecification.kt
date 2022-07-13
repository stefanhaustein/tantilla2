package org.kobjects.dialog

class DialogSpecification(
    val title: String,
    val text: String = "",
    val lines: List<InputLine<*>> = listOf(),
    val confirmLabel: String = "Ok",
    val dismissLabel: String? = null,
    val onConfirm: (List<*>) -> Unit
) {

}