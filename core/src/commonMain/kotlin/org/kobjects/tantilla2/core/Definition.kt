package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable

interface Definition : SerializableCode {
    val parentScope: Scope?
    val kind: Kind
    val name: String
    val mutable: Boolean
        get() = false

    val value: Any?
    val type: Type
        get() = value.dynamicType

    var docString: String
        get() = ""
        set(_) = throw UnsupportedOperationException()

    var index: Int
        get() = -1
        set(_) = throw UnsupportedOperationException()

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
    fun rebuild(compilationResults: CompilationResults): Boolean {
        val error = error()
        val localResult = CompilationResults.DefinitionCompilationResult(
            this,
            if (error == null) emptyList() else listOf(error),
            false)

        compilationResults.definitionCompilationResults.put(this, localResult)
        return error == null
    }

    fun serializeSummary(writer: CodeWriter)
    fun serializeTitle(writer: CodeWriter)

    enum class Kind {
        FIELD, STATIC, FUNCTION, METHOD, TRAIT, STRUCT, UNIT, IMPL, UNPARSEABLE
    }
}