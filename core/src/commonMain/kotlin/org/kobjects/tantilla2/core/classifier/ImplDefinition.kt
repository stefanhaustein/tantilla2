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

    override val supportsMethods: Boolean
        get() = true

    override fun resolve(name: String): Definition? = resolveDynamic(name, false)


    override fun rebuild(compilationResults: CompilationResults): Boolean {
        if (super.rebuild(compilationResults)) {

            val vmt = MutableList<Lambda?>(trait.traitIndex) { null }
            for (definition in trait) {
                val index = (definition.value() as TraitMethod).index
                val resolved = resolve(definition.name)
                if (resolved == null) {
                    throw RuntimeException("Can't resolve '${definition.name}' for '$name'")
                }
                vmt[index] = resolved.value() as Lambda
            }
            this.vmt = vmt.toList() as List<Lambda>

            compilationResults.classToTrait.getOrPut(classifier) { mutableMapOf() }[trait] = this
            compilationResults.traitToClass.getOrPut(trait) { mutableMapOf() }[classifier] = this

            return true
        }
        return false
    }

    override fun serializeType(writer: CodeWriter) {
        writer.append(name)
    }


}