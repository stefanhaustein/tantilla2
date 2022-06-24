package org.kobjects.tantilla2.android

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
import org.kobjects.greenspun.core.Evaluable
import org.kobjects.konsole.compose.AnsiConverter.ansiToAnnotatedString
import org.kobjects.tantilla2.console.ConsoleLoop
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.runtime.F64
import org.kobjects.tantilla2.core.runtime.RootScope
import org.kobjects.tantilla2.core.runtime.Void
import org.kobjects.tantilla2.stdlib.PenDefinition
import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Lambda
import java.lang.Exception

class TantillaViewModel(
    val console: ConsoleLoop,
    val bitmap: Bitmap,
    val exampleLoader: (String) -> String,
) {
    var editing = mutableStateOf(false)
    val mode = mutableStateOf(Mode.SHELL)
    var fileName = mutableStateOf("")
    val userScope = mutableStateOf<Scope>(console.scope)
    val builtinScope = mutableStateOf<Scope>(console.scope.parentContext!!)
    val definition = mutableStateOf<Definition?>(null)
    val currentText = mutableStateOf(TextFieldValue())
    var editorParentScope: Scope = console.scope
    val expanded = mutableStateMapOf<Definition, Unit>()
    var withRuntimeException = mutableStateMapOf<Definition, TantillaRuntimeException>()

    init {
        defineNatives()
        console.errorListener = ::highlightRuntimeException
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
        RootScope.add(Definition(
            RootScope,
            Definition.Kind.STATIC_VARIABLE,
            "pen",
            resolvedType = PenDefinition,
            resolvedValue = penImpl
        ))

        RootScope.defineNativeFunction(
            "requestAnimationFrame",
            "Calls the given function before refreshing the screen.",
            Void,
            Parameter("callback", FunctionType.Impl(Void, emptyList()))
        ) { context ->
            Choreographer.getInstance().postFrameCallback {
                val fn = context[0] as Lambda
                val functionContext = RuntimeContext(MutableList(fn.scopeSize) { null })
                fn.eval(functionContext)
            }
        }

    }

    fun scope(): MutableState<Scope> = if (mode.value == Mode.HELP) builtinScope else userScope

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
        currentText.value = currentText.value.copy(annotatedString = ansiToAnnotatedString(writer.toString()).withError(definition?.error()))
        editing.value = true
    }

    fun reset() {
        console.setUserScope(UserScope(RootScope))
        defineNatives()
        userScope.value = console.scope
        builtinScope.value = console.scope.parentContext!!
        fileName.value = ""
    }

    fun runMain() {
        mode.value = Mode.SHELL
        try {
            val definition = userScope.value["main"]
            if (definition == null) {
                console.konsole.write("main() undefined.")
                return
            }
            if (definition.type() !is FunctionType) {
                console.konsole.write("main is not a function.")
                return
            }
            val function = definition.value() as Lambda
            function.eval(RuntimeContext(MutableList(function.scopeSize) { null }))
        } catch (e: Exception) {
            e.printStackTrace()
            console.konsole.write(e.message ?: e.toString())
        }
    }

    fun loadExample(name: String) {
        val programText = exampleLoader(name)
        reset()
        this.fileName.value = name
        Parser.parse(programText, console.scope)
        console.scope.hasError()
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


    enum class Mode {
        HELP, HIERARCHY, SHELL
    }

}