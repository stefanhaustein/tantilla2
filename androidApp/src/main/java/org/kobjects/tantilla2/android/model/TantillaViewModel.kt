package org.kobjects.tantilla2.android.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.Choreographer
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import org.kobjects.dialog.DialogManager
import org.kobjects.konsole.compose.AnsiConverter.ansiToAnnotatedString
import org.kobjects.konsole.compose.ComposeKonsole
import org.kobjects.tantilla2.console.ConsoleLoop
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.runtime.F64
import org.kobjects.tantilla2.core.runtime.RootScope
import org.kobjects.tantilla2.core.runtime.Void
import org.kobjects.tantilla2.stdlib.PenDefinition
import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.android.PenImpl
import org.kobjects.tantilla2.core.classifier.NativePropertyDefinition
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Callable
import java.io.File
import java.lang.Runnable
import java.nio.charset.StandardCharsets
import java.util.concurrent.LinkedBlockingQueue
import kotlin.Exception

class TantillaViewModel(
    val console: ConsoleLoop,
    val bitmap: Bitmap,
    val platform: Platform
) : ViewModel() {
    var editing = mutableStateOf(false)
    val mode = mutableStateOf(Mode.SHELL)
    var fileName = mutableStateOf(platform.fileName)
    val userScope = mutableStateOf<UserRootScope>(console.scope)
    val builtinScope = mutableStateOf<Scope>(console.scope.parentScope)
    val definition = mutableStateOf<Definition?>(null)
    val currentText = mutableStateOf(TextFieldValue())
    var editorParentScope: Scope = console.scope
    val expanded = mutableStateOf(setOf<Definition>())
    var withRuntimeException = mutableStateMapOf<Definition, TantillaRuntimeException>()
    var compilationResults = mutableStateOf(CompilationResults())
    val dialogManager = DialogManager()
    val globalRuntimeContext = mutableStateOf(GlobalRuntimeContext())

    init {
        defineNatives()
        console.errorListener = ::highlightRuntimeException

        val file = File(platform.rootDirectory, platform.fileName)
        if (file.exists()) {
            load(file)
        }
    }

    fun clearBitmap() {
        val filler = IntArray(bitmap.width)
        for (y in 0 until bitmap.height) {
            bitmap.setPixels(filler, 0,  bitmap.width, 0, y, bitmap.width, 1)
        }
    }

    fun defineNatives() {
        RootScope.defineNativeFunction(
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


        val canvas = Canvas(bitmap)
        canvas.translate(bitmap.width / 2f, bitmap.height / 2f)
        canvas.scale(1f, -1f)
        val penImpl = PenImpl(PenDefinition, canvas)
        RootScope.add(
            NativePropertyDefinition(
            RootScope,
            Definition.Kind.STATIC,
            "pen",
            type = PenDefinition,
            getter = {penImpl}
        )
        )

        RootScope.defineNativeFunction(
            "requestAnimationFrame",
            "Calls the given function before refreshing the screen.",
            Void,
            Parameter("callback", FunctionType.Impl(Void, emptyList()))
        ) { context ->
            if (!context.globalRuntimeContext.stopRequested) {
                Choreographer.getInstance().postFrameCallback {
                    try {
                        val fn = context[0] as Callable
                        val functionContext = LocalRuntimeContext(
                            context.globalRuntimeContext,
                            fn.scopeSize,
                            closure = fn.closure
                        )
                        fn.eval(functionContext)
                    } finally {
                        context.globalRuntimeContext.activeThreads--
                    }
                }
            }
        }
    }

    fun saveAs() {
        dialogManager.showPrompt("Save As", "File Name", fileName.value) {
            saveAs(it)
        }
    }


    fun saveAs(newName: String) {
        fileName.value = newName
        platform.fileName = newName
        save()
    }


    fun scope(): MutableState<Scope> = if (mode.value == Mode.HELP) builtinScope else userScope as MutableState<Scope>

    fun findLineRange(s: AnnotatedString, lineNumber: Int): IntRange {
        var pos = 0
        var i = 0
        while (pos < s.length && i < lineNumber) {
            val newPos = s.indexOf('\n', pos)
            if (newPos == -1) {
                break
            }
            pos = newPos + 1
            i++
        }
        var end = s.indexOf('\n', pos)
        if (end == -1) {
            end = s.length
        }
        return IntRange(pos, end)
    }

    fun AnnotatedString.withError(exception: Exception?): AnnotatedString {
        if (exception is ParsingException) {
            val range = findLineRange(this, exception.token.line)
            val builder = AnnotatedString.Builder(this)
            builder.addStyle(SpanStyle(background = Color.Yellow), range.start, range.endInclusive)
            return builder.toAnnotatedString()
        }
        return this
    }

    fun edit(parent: Scope, definition: Definition?) {
        editorParentScope = parent
        this.definition.value = definition
        val writer = CodeWriter(highlighting = CodeWriter.defaultHighlighting)
        definition?.serializeCode(writer)
        currentText.value = currentText.value.copy(annotatedString = ansiToAnnotatedString(writer.toString()).withError(definition?.errors?.firstOrNull()))
        editing.value = true
    }

    fun reset() {
        clearBitmap()
        clearConsole()
        console.setUserScope(UserRootScope(RootScope))
        defineNatives()
        userScope.value = console.scope
        builtinScope.value = console.scope.parentScope!!
        saveAs("Scratch.kt")
    }

    fun stop() {
        globalRuntimeContext.value.stopRequested = true
        globalRuntimeContext.value = GlobalRuntimeContext()
    }

    fun runMain() {
        mode.value = Mode.SHELL
            try {
                globalRuntimeContext.value.activeThreads++
                userScope.value.run(globalRuntimeContext.value)
            } catch (e: Exception) {
                e.printStackTrace()
                console.konsole.write(e.message ?: e.toString())
            } finally {
                globalRuntimeContext.value.activeThreads--
            }
    }

    private fun rebuild() {
        val result = userScope.value.rebuild()
        compilationResults.value = result
    }

    fun load(file: File) {
        platform.runAsync {
            reset()
            this.fileName.value = file.name
            loadCode(file.readText())
        }
    }

    fun loadExample(name: String) {
        platform.runAsync {
            reset()
            this.fileName.value = name
            loadCode(platform.loadExample(name))
        }
    }

    private fun loadCode(code: String) {
            try {
                Parser.parse(code, console.scope)
            } catch (e: Exception) {
                dialogManager.showError(e.toString())
            }
            rebuild()

            println("Rebuilt and re-serialized code:")
            println(CodeWriter().appendCode(userScope.value).toString())

            mode.value = Mode.HIERARCHY
    }


    fun highlightRuntimeException(e: TantillaRuntimeException?) {
        withRuntimeException.values.clear()
        if (e == null) {
            return
        }
        var definition = userScope.value.findNode(e.node)
        while (definition != null) {
            withRuntimeException.put(definition, e)
            definition = definition
        }
    }

    fun save() {
        val code = CodeWriter().appendCode(userScope.value).toString()
        val file = File(platform.rootDirectory, fileName.value)
        println("Saving code to $file:")
        println(code)
        val writer = file.writer(StandardCharsets.UTF_8)
        writer.write(code)
        writer.close()
    }

    fun clearConsole() {
       (console.konsole as ComposeKonsole).entries.clear()
    }


    enum class Mode {
        HELP, HIERARCHY, SHELL
    }

}