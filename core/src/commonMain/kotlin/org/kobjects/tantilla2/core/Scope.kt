package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.parserlib.tokenizer.ParsingException
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
import org.kobjects.tantilla2.core.parser.TokenType

abstract class Scope(
    override val parentScope: Scope?,
    val definitionText: String = "",
): Definition {
    var error: Exception? = null
    val definitions: DefinitionMap = DefinitionMap(this)
    var resolved = definitionText == ""

    open val supportsMethods: Boolean
        get() = false

    open val supportsLocalVariables: Boolean
        get() = false

/*
    override val type: Type
        get() = if (this is ClassDefinition) ClassMetaType(this) else MetaType(this)

*/


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
            //            val name = oldDefinition?.name ?: "[error]"
            replacement = Unparseable(
                this,
                definitionText = newContent
            )
        }
        definitions.definitions[replacement.name] = replacement

        return replacement
    }


    fun resolveDynamic(name: String, fallBackToStatic: Boolean): Definition? {
        val result = definitions.definitions[name]
        if (result != null) {
            if (fallBackToStatic || result.isDynamic()) {
                return result
            }
        }
        val parent = parentScope
        if (this is FunctionScope && parent is FunctionScope) {
            return parent.resolveDynamic(name, fallBackToStatic)
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
        val parent = parentScope
        if (fallBackToParent && parent != null) {
            return parent.resolveStatic(name, true)
        }
        return null
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



    override val mutable: Boolean
        get() = false

    override var index: Int
        get() = -1
        set(_) = throw UnsupportedOperationException()

    private fun tokenizer(): TantillaTokenizer {
        val tokenizer = TantillaTokenizer(definitionText)
        tokenizer.consume(TokenType.BOF)
        error = null
        return tokenizer
    }

    private fun exceptionInResolve(e: Exception, tokenizer: TantillaTokenizer): Exception {
        if (e is ParsingException) {
            error = e
        } else {
            error = ParsingException(tokenizer.current, "Error in ${parentScope}.$name: " +  (e.message ?: "Parsing Error"), e)
        }
        error!!.printStackTrace()
        throw error!!
    }

    override fun error(): Exception? {
        if (error == null) {
            try {
                value()
            } catch (e: Exception) {
                println("Error in $parentScope.$name")
                e.printStackTrace()
            }
        }
        return error;
    }

    override fun rebuild(compilationResults: CompilationResults): Boolean {
        var ok = true
        if (error() != null) {
            compilationResults.errors.add(this)
            ok = false
        }
        for (definition in definitions) {
            if (!definition.rebuild(compilationResults)) {
                compilationResults.errors.add(this)
                ok = false
            } else if (definition is ImplDefinition) {
                compilationResults.classToTrait.getOrPut(definition.struct) { mutableMapOf() }[definition.trait] = this
                compilationResults.traitToClass.getOrPut(definition.trait) { mutableMapOf() }[definition.struct] = this
            }
        }
        return ok
    }

    override fun valueType(): Type = value().dynamicType

    override fun value(): Any? {
        if (!resolved) {
            val tokenizer = tokenizer()
            println("Resolving: $definitionText")
            try {
                resolve(tokenizer)
                resolved = true
            } catch (e: Exception) {
                throw exceptionInResolve(e, tokenizer)
            }
        }
        return this
    }

    open fun resolve(tokenizer: TantillaTokenizer) {
        tokenizer.consume(TokenType.IDENTIFIER)  // Kind
        tokenizer.consume(name)
        tokenizer.consume(":")
        Parser.parse(tokenizer, ParsingContext(this, 1))
    }

    override fun initializer(): Evaluable<RuntimeContext>? {
        throw IllegalStateException("Initilizer not available for $kind")
    }


    override fun toString() = name


    override fun serializeTitle(writer: CodeWriter) {
        writer.keyword(kind.name.lowercase()).append(' ').declaration(name)
    }

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
       if (!resolved) {
           writer.append(definitionText)
           return
       }

        writer.keyword(kind.name.lowercase()).append(' ')

        writer.indent()
        for (definition in definitions.definitions.values) {
            writer.newline()
            writer.newline()
            writer.appendCode(definition)
        }
        writer.outdent()
    }



    override fun serializeSummary(writer: CodeWriter) {
        serializeTitle(writer)
        writer.append(":")
        writer.indent()
        val scope = value() as Scope
        for (definition in scope.definitions.iterator()) {
            writer.newline()
            definition.serializeTitle(writer)
        }
        writer.outdent()
    }

    override fun isDynamic() = false

    override fun isScope() = error() == null


    override fun findNode(node: Evaluable<RuntimeContext>): Definition? {
        for (definition in definitions) {
            val result = definition.findNode(node)
            if (result != null) {
                return result
            }
        }
        return null
    }


    override fun depth(scope: Scope): Int {
        val parent = parentScope
        if (scope == parent) {
            return 0
        }
        if (parent == null) {
            throw IllegalStateException("Definition $this not found in scope.")
        }
        return 1 + depth(parent)
    }

}