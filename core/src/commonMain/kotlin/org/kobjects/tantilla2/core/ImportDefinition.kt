package org.kobjects.tantilla2.core

class ImportDefinition(
    override val parentScope: Scope,
    val path: List<String>
) : Definition {
    var resolved: Definition? = null

    override val name: String
        get() = path.last()

    override val kind: Definition.Kind
        get() = Definition.Kind.IMPORT

    override fun getValue(self: Any?): Definition = resolve()

    fun resolve(): Definition {
        if (resolved == null) {
            var result = parentScope.resolveStatic(path[0], true)
            for (i in 1 until path.size) {
                result = (result as Scope).resolveStatic(path[i], false)
            }
            resolved = result
        }
        return resolved!!
    }

    override fun serializeSummary(writer: CodeWriter) = serializeCode(writer)

    override fun serializeTitle(writer: CodeWriter, abbreviated: Boolean) = serializeCode(writer)

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.appendKeyword("import ").append(path.joinToString("."))
    }
}