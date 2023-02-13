package org.kobjects.tantilla2.core.classifier

import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.classifier.TraitDefinition.Companion.vmtIndex
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionDefinition

abstract class ImplDefinition(
    override val parentScope: Scope,
    override var docString: String,
) : Classifier() {

    abstract val trait: TraitDefinition

    abstract val scope: Scope

    var vmt = emptyList<Callable>()

    override val supportsMethods: Boolean
        get() = true

    override fun invalidate() {
        super.invalidate()
        vmt = emptyList()
    }


    override fun resolve(applyOffset: Boolean, errorCollector: MutableList<ParsingException>?) {
        // TODO: Move VMT creation to trait?
        val vmt = Array<Callable?>(trait.traitIndex) { null }
        for (definition in trait) {
            val index = definition.vmtIndex
            val resolved = resolve(definition.name)
                ?: throw RuntimeException("Can't resolve '${definition.name}' for '${this.name}'")
            vmt[index] = resolved.getValue(null) as Callable
        }
        this.vmt = vmt.toList() as List<Callable>
    }

    override val kind: Definition.Kind
        get() = Definition.Kind.IMPL




}