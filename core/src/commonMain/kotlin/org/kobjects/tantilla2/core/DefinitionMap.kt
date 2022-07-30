package org.kobjects.tantilla2.core

class DefinitionMap(val scope: Scope): Iterable<Definition> {
    val definitions = mutableMapOf<String, Definition>()
    var locals = mutableListOf<String>()


    operator fun get(name: String): Definition? = definitions[name]

    override fun iterator(): Iterator<Definition> = definitions.values.iterator()

    fun add(definition: Definition) {
        definitions[definition.name] = definition
        if (definition.kind == Definition.Kind.PROPERTY && definition.index == -1) {
            definition.index = locals.size
            locals.add(definition.name)
        }
    }


    fun remove(name: String) {
        val removed = definitions.remove(name)
        if (removed != null && removed.index != -1) {
            locals.remove(name)
            for (definition in this) {
                if (definition.index > removed.index) {
                    definition.index--
                }
            }
        }
    }
}