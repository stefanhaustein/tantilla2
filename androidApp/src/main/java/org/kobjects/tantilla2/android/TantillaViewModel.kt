package org.kobjects.tantilla2.android

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import org.kobjects.greenspun.core.F64
import org.kobjects.greenspun.core.Type
import org.kobjects.greenspun.core.Void
import org.kobjects.tantilla2.console.ConsoleLoop
import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.runtime.RootScope
import org.kobjects.tantilla2.stdlib.PenDefinition
import org.kobjects.tantilla2.stdlib.StdLib

class TantillaViewModel(
    val console: ConsoleLoop,
    val bitmap: Bitmap,
    val exampleLoader: (String) -> String,
) {
    var editing = mutableStateOf(false)
    val mode = mutableStateOf(Mode.SHELL)
    var fileName = mutableStateOf("")
    val userScope = mutableStateOf<Scope>(console.scope)
    val builtinScope = mutableStateOf<Scope>(console.scope)
    val definition = mutableStateOf<Definition?>(null)
    val currentText = mutableStateOf("")
    var editorParentScope: Scope = console.scope
    val showDocString = mutableStateOf<Definition?>(null)

    init {
        defineNatives()
    }

    fun defineNatives() {
        console.scope.defineNative(
            "setPixel",
            "Sets the pixel at the given x/y coordinate to the given 32bit color value in ARGB format.",
            Void,
            Parameter("x", F64),
            Parameter("y", F64),
            Parameter("color", F64)
        ) {
            bitmap.setPixel(
                (it.variables[0] as Double).toInt(),
                (it.variables[1] as Double).toInt(),
                (it.variables[2] as Double).toInt())
        }

        StdLib.setup(console.scope)
        val penDefinition = console.scope.definitions["Pen"]!!.value() as Type

        val penIndex = console.scope.declareLocalVariable(
            "pen", penDefinition, false, true)
        while (console.runtimeContext.variables.size < penIndex) {
            console.runtimeContext.variables.add(null)
        }
        val canvas = Canvas(bitmap)
        canvas.translate(bitmap.width / 2f, bitmap.height / 2f)
        canvas.scale(1f, -1f)

        console.runtimeContext.variables.add(PenImpl(penDefinition, canvas))
    }

    fun scope(): MutableState<Scope> = if (mode.value == Mode.HELP) builtinScope else userScope

    fun edit(parent: Scope, definition: Definition?) {
        editorParentScope = parent
        this.definition.value = definition
        currentText.value = definition?.serialize() ?: ""
        editing.value = true
    }

    fun reset() {
        console.setRootScope(RootScope())
        defineNatives()
        userScope.value = console.scope
        builtinScope.value = console.scope
        fileName.value = ""
    }

    fun loadExample(name: String) {
        val programText = exampleLoader(name)
        reset()
        this.fileName.value = name
        Parser.parse(programText, console.scope)
    }

    enum class Mode {
        HELP, HIERARCHY, SHELL
    }

}