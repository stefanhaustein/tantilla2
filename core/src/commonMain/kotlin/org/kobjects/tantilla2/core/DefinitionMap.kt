package org.kobjects.tantilla2.core

class DefinitionMap(val scope: Scope): Iterable<Definition> {
    val definitions = mutableMapOf<String, Definition>()
    var locals = mutableListOf<String>()


    operator fun get(name: String): Definition? = definitions[name]

    override fun iterator(): Iterator<Definition> = definitions.values.iterator()

    fun add(definition: Definition) {
        definitions[definition.name] = definition
        if (definition.kind == Definition.Kind.FIELD && definition.index == -1) {
            definition.index = locals.size
            locals.add(definition.name)
        }
    }


}