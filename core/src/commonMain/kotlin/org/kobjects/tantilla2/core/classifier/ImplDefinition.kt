package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionDefinition
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.parser.ParsingContext
import org.kobjects.tantilla2.core.parser.TantillaTokenizer
import org.kobjects.tantilla2.core.parser.TokenType

class ImplDefinition(
    override val parentScope: Scope,
    override val name: String,
    val definitionText: String,
    override var docString: String,
) : Scope(), Type {
    var vmt = listOf<Callable>()

    var resolvedTrait: TraitDefinition? = null
    var resolvedScope: Scope? = null

    val trait: TraitDefinition
        get() {
            getValue(null)
            return resolvedTrait!!
        }

    val scope: Scope
        get() {
            getValue(null)
            return resolvedScope!!
        }

    override val supportsMethods: Boolean
        get() = true

    override fun resolve(name: String): Definition? = resolveDynamic(name, false)

    override fun resolveAll(compilationResults: CompilationResults): Boolean {
        if (super.resolveAll(compilationResults)) {

            val vmt = MutableList<Callable?>(trait.traitIndex) { null }
            for (definition in trait) {
                val index = ((definition.getValue(null) as FunctionDefinition).resolvedBody as TraitMethodBody).index
                val resolved = resolve(definition.name)
                if (resolved == null) {
                    throw RuntimeException("Can't resolve '${definition.name}' for '${this.name}'")
                }
                vmt[index] = resolved.getValue(null) as Callable
            }
            this.vmt = vmt.toList() as List<Callable>

            return true
        }
        return false
    }

    override fun serializeType(writer: CodeWriter) {
        writer.append(this.name)
    }


    override fun getValue(scope: Any?): ImplDefinition {
            if (resolvedTrait == null) {
                val traitName = name.substring(0, name.indexOf(' '))
                resolvedTrait =
                    parentScope.resolveStatic(traitName, true)!!.getValue(null) as TraitDefinition
                val className = name.substring(name.lastIndexOf(' ') + 1)
                resolvedScope =
                    parentScope.resolveStatic(className, true)!!.getValue(null) as Scope

                val tokenizer = TantillaTokenizer(definitionText)
                tokenizer.consume(TokenType.BOF)
                tokenizer.consume("impl")
                tokenizer.consume(traitName)
                tokenizer.consume("for")
                tokenizer.consume(className)
                tokenizer.consume(":")
                Parser.parseDefinitionsAndStatements(tokenizer, ParsingContext(this, 0))
                println("Impl successfully resolved!")
            }
            return this
        }

    override val kind: Definition.Kind
        get() = Definition.Kind.IMPL


}