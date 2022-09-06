package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable

interface Definition : SerializableCode, Comparable<Definition> {
    val parentScope: Scope?
    val kind: Kind
    val name: String
    val mutable: Boolean
        get() = false

    val type: Type
        get() = getValue(null).dynamicType

    var docString: String
        get() = ""
        set(_) = throw UnsupportedOperationException()

    var index: Int
        get() = -1
        set(_) = throw UnsupportedOperationException()

    val errors: List<Exception>
        get() = emptyList()

    fun getValue(self: Any?): Any?

    fun setValue(self: Any?, newValue: Any?): Unit = throw UnsupportedOperationException()

    fun depth(scope: Scope): Int {
        if (scope == this.parentScope) {
            return 0
        }
        if (scope.parentScope == null) {
            throw IllegalStateException("Definition $this not found in scope.")
        }
        return 1 + depth(scope.parentScope!!)
    }

    fun findNode(node: Evaluable<LocalRuntimeContext>): Definition? = null
    fun isDynamic() = kind == Kind.METHOD || kind == Kind.PROPERTY
    fun isScope(): Boolean = false



    fun resolveAll(compilationResults: CompilationResults): Boolean {
        val ok = errors.isEmpty()
        if (!ok) {
            compilationResults.definitionsWithErrors.add(this)
        }
        return ok
    }

    /**
     * Reset the compilation state
     */
    fun reset() {
    }


    fun serializeSummary(writer: CodeWriter)
    fun serializeTitle(writer: CodeWriter, abbreviated: Boolean = false)

    enum class Kind {
        IMPORT, STATIC, FUNCTION, PROPERTY, METHOD, TRAIT, STRUCT, UNIT, IMPL, UNPARSEABLE
    }


    override fun compareTo(other: Definition): Int {
        var d = kind.compareTo(other.kind)
        if (d != 0) {
            return d
        }
        d = index.compareTo(other.index)
        if (d != 0) {
            return d
        }
        return name.compareTo(other.name)
    }

    fun userRootScope(): UserRootScope {
        var current = parentScope
        while (current !is UserRootScope) {
            current = current?.parentScope ?: throw RuntimeException("User root scope not found.")
        }
        return current
    }

}