package org.kobjects.tantilla2.core.classifier

import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.TraitMethod

class ImplDefinition(
    override val name: String,
    parentContext: Scope?,
    val trait: TraitDefinition,
    val classifier: ClassDefinition,
) : Scope(parentContext), Type {
    var vmt = listOf<Callable>()

    override fun resolveAll() {
        trait.resolveAll()
        classifier.resolveAll()

        super.resolveAll()

        val vmt = MutableList<Callable?>(trait.traitIndex) { null }
        for (definition in trait.definitions.values) {
            val index = (definition.value() as TraitMethod).index
            val target = resolve(definition.name).value() as Callable
            vmt[index] = target
        }
        this.vmt = vmt.toList() as List<Callable>
    }

}