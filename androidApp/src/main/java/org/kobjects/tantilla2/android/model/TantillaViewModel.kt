package org.kobjects.tantilla2.android.model

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import org.kobjects.dialog.DialogManager
import org.kobjects.dialog.InputLine
import org.kobjects.konsole.compose.AnsiConverter.ansiToAnnotatedString
import org.kobjects.konsole.compose.ComposeKonsole
import org.kobjects.tantilla2.console.ConsoleLoop
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.runtime.RootScope
import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.function.FunctionDefinition
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.Exception

class TantillaViewModel(
    val console: ConsoleLoop,
    val bitmap: Bitmap,
    val platform: Platform
) : ViewModel() {
    val mode = mutableStateOf(Mode.SHELL)
    var fileName = mutableStateOf(platform.fileName)
    val userScope = mutableStateOf<Scope>(console.scope)
    val helpScope = mutableStateOf<Scope>(console.scope.parentScope)
    val editingDefinition = mutableStateOf<Definition?>(null)
    val currentText = mutableStateOf(TextFieldValue())
    val expanded = mutableStateOf(setOf<Definition>())
    var withRuntimeException = mutableStateMapOf<Definition, TantillaRuntimeException>()
    val dialogManager = DialogManager()
    val globalRuntimeContext = mutableStateOf(GlobalRuntimeContext())
    val forceUpdate = mutableStateOf(0)
    val graphicsUpdateTrigger = mutableStateOf(0)
    val userRootScope
        get() = console.scope

    init {
        defineNatives(bitmap, graphicsUpdateTrigger)
        console.errorCallback = ::highlightRuntimeException

        val file = File(platform.rootDirectory, platform.fileName)
        if (file.exists()) {
            load(file)
        } else if (platform.fileName == "Scratch.tt") {
            reset(true)
        } else {
            reset(false)
        }
    }

    fun clearBitmap() {
        val filler = IntArray(bitmap.width)
        for (y in 0 until bitmap.height) {
            bitmap.setPixels(filler, 0,  bitmap.width, 0, y, bitmap.width, 1)
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


    fun scope(): MutableState<Scope> = if (mode.value == Mode.HELP) helpScope else userScope as MutableState<Scope>

    fun add(kind: Definition.Kind) {

        when (kind) {
            Definition.Kind.FUNCTION -> {
                if (userScope.value.supportsMethods) {
                    dialogManager.showCustom(
                        "Add Function",
                        "",
                        listOf(InputLine("Static", false), InputLine("Name", ""))
                    ) {
                        add("${if (it[0] == true) "static " else "" }def ${it[1]}():") }
                } else {
                    dialogManager.showPrompt("Add Function", "Name", "") {
                        add("def $it():")
                    }
                }
            }
            Definition.Kind.PROPERTY -> {
                if (userScope.value.supportsLocalVariables) {
                    dialogManager.showCustom(
                        "Add Field",
                        "",
                        listOf(InputLine("Static", false), InputLine("Name", ""))
                    ) {
                        add("${if (it[0] == true) "static " else "" }${it[1]} =") }
                } else {
                    dialogManager.showPrompt("Add Field", "Name", "") {
                        add("$it = ")
                    }
                }
            }
            Definition.Kind.IMPL -> add("impl ")
            else -> {
                val lowercase = kind.toString().lowercase()
                dialogManager.showPrompt("Add $lowercase", "Name", "") {
                    add("$lowercase $it:")
                }
            }
        }

    }

    fun add(text: String) {
        this.editingDefinition.value = null
        currentText.value = currentText.value.copy(annotatedCode(text, emptyList()))
        mode.value = Mode.DEFINITION_EDITOR
    }


    fun edit(definition: Definition?) {
        this.editingDefinition.value = definition
        val writer = CodeWriter()
        definition?.serializeCode(writer)
        currentText.value = currentText.value.copy(
            annotatedString = annotatedCode(writer.toString(), definition?.errors ?: emptyList()))
        mode.value = Mode.DEFINITION_EDITOR
    }


    fun editDocumentation() {
        currentText.value = currentText.value.copy(text = userScope.value.docString)
        mode.value = Mode.DOCUMENTATION_EDITOR
    }


    fun confirmReset() {
        dialogManager.showConfirmation("Full Reset", "Start from scratch? The current program will be deleted.") {
            reset(addHello = true)
        }
    }

    fun reset(addHello: Boolean = false) {
        clearBitmap()
        clearConsole()
        val newRoot = UserRootScope(RootScope)
        console.setUserScope(newRoot)
        defineNatives(bitmap, graphicsUpdateTrigger)
        userScope.value = newRoot
        helpScope.value = newRoot.parentScope!!

        if (addHello) {
            newRoot.add(
                FunctionDefinition(
                    newRoot,
                    Definition.Kind.FUNCTION,
                    "main",
                    "def main():\n  print(\"Hello World!\")"
                )
            )
            saveAs("Scratch.tt")
        }
    }

    fun stop() {
        globalRuntimeContext.value.stopRequested = true
        globalRuntimeContext.value = GlobalRuntimeContext()
    }

    fun runMain() {
        mode.value = Mode.SHELL
            try {
                globalRuntimeContext.value.activeThreads++
                userRootScope.run(globalRuntimeContext.value)
            } catch (e: Exception) {
                e.printStackTrace()
                console.konsole.write(e.message ?: e.toString())
            } finally {
                globalRuntimeContext.value.activeThreads--
            }
    }

    private fun rebuild() {
        userRootScope.rebuild()
    }

    fun load(file: File) {
        platform.runAsync {
            reset()
            this.fileName.value = file.name
            platform.fileName = file.name
            loadCode(file.readText())
        }
    }

    fun loadExample(name: String) {
        platform.runAsync {
            reset()
            this.fileName.value = name
            platform.fileName = name
            loadCode(platform.loadExample(name))
        }
    }

    private fun loadCode(code: String) {
            try {
                Parser.parseProgram(code, console.scope)
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
        HELP, HIERARCHY, SHELL, DEFINITION_EDITOR, DOCUMENTATION_EDITOR
    }

    companion object {
        fun annotatedCode(code: String, errors: List<Exception>) =
            ansiToAnnotatedString(highlightSyntax(code, errors.filterIsInstance<ParsingException>(), CodeWriter.defaultHighlighting))


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


    }
}