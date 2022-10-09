package org.kobjects.tantilla2.android.model

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
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
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.builtin.RootScope
import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.function.FunctionDefinition
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.Exception

class TantillaViewModel(
    val konsole: ComposeKonsole,
    val bitmap: Bitmap,
    val platform: Platform
) : ViewModel() {
    val rootScope = RootScope(platform, ::runStateCallback)
    var userRootScope = UserRootScope(rootScope)
    var globalRuntimeContext = GlobalRuntimeContext(userRootScope)

    val mode = mutableStateOf(Mode.SHELL)
    var fileName = mutableStateOf(platform.fileName)
    val userScope = mutableStateOf<Scope>(userRootScope)
    val helpScope = mutableStateOf<Scope>(rootScope)
    val editingDefinition = mutableStateOf<Definition?>(null)
    val currentText = mutableStateOf(TextFieldValue())
    val expanded = mutableStateOf(setOf<Definition>())
    var runtimeException = mutableStateOf<TantillaRuntimeException?>(null)
    val dialogManager = DialogManager()

    val graphicsUpdateTrigger = mutableStateOf(0)
    val runstateUpdateTrigger = mutableStateOf(0)
    val codeUpdateTrigger = mutableStateOf(0)


    var runtimeExceptionPosition = IntRange(-1, 0)

    val graphicsSystem = defineNatives(rootScope, bitmap, graphicsUpdateTrigger)

    init {
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
        notifyCodeChangedAndSave()
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
        val errorNode = if (definition == runtimeException.value?.definition) runtimeException.value?.node else null
        val writer = CodeWriter(errorNode = errorNode)
        definition?.serializeCode(writer)
        runtimeExceptionPosition = IntRange(writer.errorPosition, writer.errorPosition + writer.errorLength)
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
        userRootScope = UserRootScope(rootScope)
        globalRuntimeContext = GlobalRuntimeContext(userRootScope)

        userScope.value = userRootScope
        helpScope.value = rootScope

        if (addHello) {
            userRootScope.add(
                FunctionDefinition(
                    userRootScope,
                    Definition.Kind.FUNCTION,
                    "main",
                    "def main():\n  print(\"Hello World!\")"
                )
            )
            saveAs("Scratch.tt")
        }
    }

    fun stop() {
        globalRuntimeContext.stopRequested = true
        runstateUpdateTrigger.value++
    }

    fun runMain() {
        mode.value = Mode.SHELL
        globalRuntimeContext.run()
        runstateUpdateTrigger.value++
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
                Parser.parseProgram(code, userRootScope)
            } catch (e: Exception) {
                dialogManager.showError(e.toString())
            }
            rebuild()

            println("Rebuilt and re-serialized code:")
            println(CodeWriter().appendCode(userScope.value).toString())

            mode.value = Mode.HIERARCHY
    }


    fun runStateCallback(globalRuntimeContext: GlobalRuntimeContext) {
        runstateUpdateTrigger.value++
        val e = globalRuntimeContext.exception
        runtimeException.value = e
        if (e == null) {
            return
        }
        var definition = e.definition
        if (definition is FunctionDefinition) {
            edit(definition)
        } else if (definition?.parentScope != null){
            userScope.value = definition.parentScope!!
            mode.value = Mode.HIERARCHY
        } else {
            konsole.write(e.message ?: e.toString())
        }
    }

    fun notifyCodeChangedAndSave() {
        val code = CodeWriter().appendCode(userScope.value).toString()
        val file = File(platform.rootDirectory, fileName.value)
        println("Saving code to $file:")
        println(code)
        val writer = file.writer(StandardCharsets.UTF_8)
        writer.write(code)
        writer.close()
        codeUpdateTrigger.value++
        runtimeException.value = null
    }

    fun clearConsole() {
       konsole.entries.clear()
    }


    enum class Mode {
        HELP, HIERARCHY, SHELL, DEFINITION_EDITOR, DOCUMENTATION_EDITOR
    }

    fun annotatedCode(code: String, errors: List<Exception>): AnnotatedString {

        val writer = CodeWriter("", CodeWriter.defaultHighlighting)
        highlightSyntax(writer, code, errors, runtimeException.value, runtimeExceptionPosition)

        return ansiToAnnotatedString(writer.toString())
    }

    fun onTap(x: Double, y: Double) = globalRuntimeContext.onTap(x, y)


    suspend fun consoleLoop() {
        while (true) {
            val input = konsole.read()
            globalRuntimeContext.processShellInput(input)
        }
    }


    companion object {


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