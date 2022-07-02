package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.Unparseable
import org.kobjects.tantilla2.core.classifier.UserClassDefinition
import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.function.FunctionScope
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunction
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.parser.ParsingContext
import org.kobjects.tantilla2.core.parser.TantillaTokenizer

abstract class Scope(
    val parentScope: Scope?
): SerializableCode {
    val definitions: DefinitionMap = DefinitionMap(this)
    abstract val name: String

    open val supportsMethods: Boolean
        get() = false

    open val supportsLocalVariables: Boolean
        get() = false

/*
    override val type: Type
        get() = if (this is ClassDefinition) ClassMetaType(this) else MetaType(this)

*/

    fun findNode(node: Evaluable<RuntimeContext>): Definition? {
        for (definition in definitions) {
            val result = definition.findNode(node)
            if (result != null) {
                return result
            }
        }
        return null
    }


    fun declareLocalVariable(name: String, type: Type, mutable: Boolean): Int {
        val definition = VariableDefinition(
            this,
            Definition.Kind.FIELD,
            name,
            mutable = mutable,
            resolvedType = type
        )
        definitions.add(definition)
        return definition.index
    }


    fun update(newContent: String, oldDefinition: Definition? = null): Definition {
        if (oldDefinition != null) {
            definitions.definitions.remove(oldDefinition.name)
        }
        val tokenizer = TantillaTokenizer(newContent)
        tokenizer.next()
        var replacement: Definition
        try {
            replacement = Parser.parseDefinition(tokenizer, ParsingContext(this, 0))
        } catch (e: Exception) {
            e.printStackTrace()
            val name = oldDefinition?.name ?: "[error]"
            replacement = Unparseable(
                this,
                definitionText = newContent
            )
        }
        definitions.definitions[replacement.name] = replacement

        return replacement
    }


    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        val sb = StringBuilder()

        when (this) {
            is UserClassDefinition -> writer.keyword("struct").append(' ').declaration(name)
            is TraitDefinition -> writer.keyword("trait").append(' ').declaration(name)
            is ImplDefinition -> writer.keyword("impl").append(' ').declaration(name)
            is FunctionScope -> {
                sb.append("lambda ")
            }
        }
        writer.indent()
        for (definition in definitions.definitions.values) {
            writer.newline()
            writer.newline()
            writer.appendCode(definition)
        }
        writer.outdent()
    }

    fun resolveDynamic(name: String, fallBackToStatic: Boolean): Definition? {
        val result = definitions.definitions[name]
        if (result != null) {
            if (fallBackToStatic || result.isDynamic()) {
                return result
            }
        }
        if (this is FunctionScope && parentScope is FunctionScope) {
            return parentScope.resolveDynamic(name, fallBackToStatic)
        }
        
        if (fallBackToStatic) {
            return resolveStatic(name, true)
        }
        return null
    }

    fun resolveStatic(name: String, fallBackToParent: Boolean = false): Definition? {
        val result = definitions.definitions[name]
        if (result != null) {
            if (result.isDynamic()) {
                throw RuntimeException("Static property expected; found: $result")
            }
            return result
        }
        if (fallBackToParent && parentScope != null) {
            return parentScope.resolveStatic(name, true)
        }
        return null
    }

    open fun rebuild(compilationResults: CompilationResults): Boolean {
        var ok = true
        for (definition in definitions) {
            ok = definition.rebuild(compilationResults) && ok
        }
        return ok
    }

    fun defineNativeFunction(
        name: String,
        docString: String,
        returnType: Type,
        vararg parameter: Parameter,
        operation: (RuntimeContext) -> Any?) {
        val type = object : FunctionType {
            override val returnType = returnType
            override val parameters = parameter.toList()
        }
        val function = NativeFunction(type, operation)
        definitions.definitions[name] = FunctionDefinition(
            this,
            if (parameter.isNotEmpty() && parameter[0].name == "self") Definition.Kind.METHOD else Definition.Kind.FUNCTION,
            name,
            resolvedType = type,
            resolvedValue = function,
            docString = docString
        )
    }

    fun remove(name: String) {
        val removed = definitions.definitions.remove(name)
        if (removed != null && removed.index != -1) {
            definitions.locals.remove(name)
            for (definition in definitions.iterator()) {
                if (definition.index > removed.index) {
                    definition.index--
                }
            }
        }
    }

    override fun toString() = name

}