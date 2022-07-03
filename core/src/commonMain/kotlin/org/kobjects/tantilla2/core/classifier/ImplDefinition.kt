package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.Lambda
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.parser.ParsingContext
import org.kobjects.tantilla2.core.parser.TantillaTokenizer

class ImplDefinition(
    parentContext: Scope,
    override val name: String,
    definitionText: String,
    override var docString: String,
) : Scope(parentContext, definitionText), Type {
    var vmt = listOf<Lambda>()

    var resolvedTrait: TraitDefinition? = null
    var resolvedStruct: UserClassDefinition? = null

    val trait: TraitDefinition
        get() {
            value()
            return resolvedTrait!!
        }

    val struct: UserClassDefinition
        get() {
            value()
            return resolvedStruct!!
        }

    override val supportsMethods: Boolean
        get() = true

    override fun resolve(name: String): Definition? = resolveDynamic(name, false)


    override fun rebuild(compilationResults: CompilationResults): Boolean {
        if (super.rebuild(compilationResults)) {

            val vmt = MutableList<Lambda?>(trait.traitIndex) { null }
            for (definition in trait.definitions) {
                val index = (definition.value() as TraitMethod).index
                val resolved = resolve(definition.name)
                if (resolved == null) {
                    throw RuntimeException("Can't resolve '${definition.name}' for '${this.name}'")
                }
                vmt[index] = resolved.value() as Lambda
            }
            this.vmt = vmt.toList() as List<Lambda>

            return true
        }
        return false
    }

    override fun serializeType(writer: CodeWriter) {
        writer.append(this.name)
    }


    override fun resolve(tokenizer: TantillaTokenizer) {

        val traitName = name.substring(0, name.indexOf(' '))
        resolvedTrait = parentScope!!.resolveStatic(traitName, true)!!.value() as TraitDefinition
        val className = name.substring(name.lastIndexOf(' ') + 1)
        resolvedStruct = parentScope!!.resolveStatic(className, true)!!.value() as UserClassDefinition

        tokenizer.consume("impl")
        tokenizer.consume(traitName)
        tokenizer.consume("for")
        tokenizer.consume(className)
        tokenizer.consume(":")
        Parser.parse(tokenizer, ParsingContext(this, 0))
        println("Impl successfully resolved!")

    }

    override val kind: Definition.Kind
        get() = Definition.Kind.IMPL


}