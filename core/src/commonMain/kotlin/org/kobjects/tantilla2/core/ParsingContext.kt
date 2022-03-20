package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type
import org.kobjects.greenspun.core.Void

class ParsingContext(
    override val name: String,
    val kind: Kind,
    val parentContext: ParsingContext?
): Type, Lambda {
    override val parameters = mutableListOf<Parameter>()
    val definitions = mutableMapOf<String, Definition>()
    var localCount = 0
    var body: Evaluable<RuntimeContext>? = null
    override var returnType: Type = Void

    override fun eval(context: RuntimeContext) = body!!.eval(context)

    fun declareLocalVariable(name: String, type: Type, mutable: Boolean, asParameter: Boolean): Int {
        val definition = Definition(
            name,
            Definition.Kind.LOCAL_VARIABLE,
            type = type,
            index = localCount,
            mutable = false)
        definitions[name] = definition
        if (asParameter) {
            if (localCount != parameters.size) {
                throw IllegalStateException("Can't declare parameter after local variable")
            }
            parameters.add(Parameter(name, type))
        }
        return localCount++
    }

    fun defineValue(name: String, value: Any) {
        definitions[name] = Definition(name, Definition.Kind.CONST, value = value)
    }

    fun defineFunction(name: String, definition: String) {
        definitions[name] = Definition(name, Definition.Kind.FUNCTION, definitionText = definition)
    }

    fun defineClass(name: String, definition: String) {
        definitions[name] = Definition(name, Definition.Kind.CLASS, definitionText = definition)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (entry in definitions.entries) {
            sb.append(entry.value).append('\n')
        }
        if (body != null) {
            sb.append(body)
        }

        return sb.toString()
    }

    fun resolve(name: String): Definition {
        return definitions[name] ?: (parentContext?.resolve(name)
            ?: throw RuntimeException("Undefined: '$name'"))
    }

    enum class Kind {
        ROOT, CLASS, FUNCTION, METHOD
    }


}