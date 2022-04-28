package org.kobjects.tantilla2.android

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import org.kobjects.konsole.compose.ComposeKonsole
import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.Scope

class TantillaViewModel(
    scope: Scope,
    val konsole: ComposeKonsole,
    val bitmap: Bitmap,
) {
    val userScope = ScopeDetails(scope)
    val builtinScope = ScopeDetails(scope)
    val mode = mutableStateOf(Mode.INTERACTIVE)
    val definition = mutableStateOf<Definition?>(null)
    val currentText = mutableStateOf("")

    enum class Mode {
        INTERACTIVE, USER_SCOPE, BUILTIN_SCOPE, EDIT
    }

    fun scopeDetails(): ScopeDetails = if (mode.value == Mode.BUILTIN_SCOPE) builtinScope else userScope

    fun edit(definition: Definition) {
       this.definition.value = definition
       currentText.value = definition.serialize()
       mode.value = Mode.EDIT
    }


    class ScopeDetails(scope: Scope) {
        val scope = mutableStateOf(scope)
        val title = mutableStateOf("Root")
    }

}