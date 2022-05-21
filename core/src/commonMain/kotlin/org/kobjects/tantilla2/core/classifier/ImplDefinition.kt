package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.SerializableType
import org.kobjects.tantilla2.core.TraitMethod
import org.kobjects.tantilla2.core.function.Lambda

class ImplDefinition(
    val name: String,
    parentContext: Scope?,
    val trait: TraitDefinition,
    val classifier: UserClassDefinition,
) : Scope(parentContext), SerializableType {
    var vmt = listOf<Lambda>()

    override val title: String
        get() = name

    override fun resolveAll() {
        trait.resolveAll()
        classifier.resolveAll()

        super.resolveAll()

        val vmt = MutableList<Lambda?>(trait.traitIndex) { null }
        for (definition in trait.definitions.values) {
            val index = (definition.value() as TraitMethod).index
            vmt[index] = resolve(definition.name).value() as Lambda
        }
        this.vmt = vmt.toList() as List<Lambda>
    }

    override fun serializeType(writer: CodeWriter) {
        writer.append(name)
    }


}