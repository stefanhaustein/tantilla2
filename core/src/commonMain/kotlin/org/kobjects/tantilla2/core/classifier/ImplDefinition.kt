package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionDefinition
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.parser.ParsingContext
import org.kobjects.tantilla2.core.parser.TantillaTokenizer
import org.kobjects.tantilla2.core.parser.TokenType
import org.kobjects.tantilla2.core.type.Type

class ImplDefinition(
    override val parentScope: Scope,
    val traitName: String,
    val scopeName: String,
    //val definitionText: String,
    override var docString: String,
) : Scope(), Type {
    override val name: String
        get() = "$traitName for $scopeName"

    var vmt = listOf<Callable>()

    var resolvedTrait: TraitDefinition? = null
    var resolvedScope: Scope? = null

    val trait: TraitDefinition
        get() {
            if (resolvedTrait == null) {
                resolvedTrait = parentScope.resolveStaticOrError(traitName, true).getValue(null) as TraitDefinition
            }
            return resolvedTrait!!
        }

    val scope: Scope
        get() {
            if (resolvedScope == null) {
                resolvedScope = parentScope.resolveStaticOrError(scopeName, true).getValue(null) as Scope
            }
            return resolvedScope!!
        }

    override val supportsMethods: Boolean
        get() = true

    override fun resolve(name: String): Definition? = resolveDynamic(name, false)

    // This can't be in "simple" resolve as it needs to update the compilation results
    override fun resolveAll(compilationResults: CompilationResults): Boolean {
        if (super.resolveAll(compilationResults)) {

            // TODO: Move VMT creation to trait?
            val vmt = Array<Callable?>(trait.traitIndex) { null }
            for (definition in trait) {
                val index = ((definition.getValue(null) as FunctionDefinition).resolvedBody as TraitMethodBody).index
                val resolved = resolve(definition.name)
                    ?: throw RuntimeException("Can't resolve '${definition.name}' for '${this.name}'")
                vmt[index] = resolved.getValue(null) as Callable
            }
            this.vmt = vmt.toList() as List<Callable>

            compilationResults.classToTrait.getOrPut(scope) { mutableMapOf() }[trait] = this
            compilationResults.traitToClass.getOrPut(trait) { mutableMapOf() }[scope] = this

            return true
        }
        return false
    }

    override fun serializeType(writer: CodeWriter) {
        writer.append(this.name)
    }


    override fun getValue(scope: Any?): ImplDefinition {
        if (resolvedTrait == null) {
            trait
            scope
        }
        return this
    }

    override val kind: Definition.Kind
        get() = Definition.Kind.IMPL


}