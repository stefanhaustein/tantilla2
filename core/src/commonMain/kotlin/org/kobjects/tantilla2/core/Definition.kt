package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable

interface Definition : SerializableCode {
    val parentScope: Scope?
    val kind: Kind
    val name: String
    val mutable: Boolean
        get() = false

    var docString: String
    var index: Int
        get() = -1
        set(_) = throw UnsupportedOperationException()

    fun value(): Any?
    fun valueType(): Type
    fun error(): Exception? = null
    fun depth(scope: Scope): Int {
            if (scope == this.parentScope) {
                return 0
            }
            if (scope.parentScope == null) {
                throw IllegalStateException("Definition $this not found in scope.")
            }
            return 1 + depth(scope.parentScope!!)
    }

    fun findNode(node: Evaluable<RuntimeContext>): Definition? = null
    fun isDynamic() = kind == Kind.METHOD || kind == Kind.FIELD
    fun isScope(): Boolean = false
    fun rebuild(compilationResults: CompilationResults): Boolean

    fun serializeSummary(writer: CodeWriter)
    fun serializeTitle(writer: CodeWriter)

    enum class Kind {
        FIELD, STATIC, FUNCTION, METHOD, TRAIT, STRUCT, UNIT, IMPL, UNPARSEABLE
    }
}