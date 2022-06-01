package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.Lambda

class ImplDefinition(
    val name: String,
    parentContext: Scope?,
    val trait: TraitDefinition,
    val classifier: UserClassDefinition,
) : Scope(parentContext), Type {
    var vmt = listOf<Lambda>()

    override val title: String
        get() = name


    override fun resolve(name: String): Definition = resolveDynamic(name, false)


    override fun hasError(): Boolean {
        if (!trait.hasError()
                    && !classifier.hasError()
                && !super.hasError()) {

            val vmt = MutableList<Lambda?>(trait.traitIndex) { null }
            for (definition in trait) {
                val index = (definition.value() as TraitMethod).index
                vmt[index] = resolve(definition.name).value() as Lambda
            }
            this.vmt = vmt.toList() as List<Lambda>
            return false
        }
        return true
    }

    override fun serializeType(writer: CodeWriter) {
        writer.append(name)
    }


}