package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable

interface Definition : SerializableCode {
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

    fun findNode(node: Evaluable<RuntimeContext>): Definition? = null
    fun isDynamic() = kind == Kind.METHOD || kind == Kind.FIELD
    fun isScope(): Boolean = false
    fun rebuild(compilationResults: CompilationResults): Boolean {
        val localResult = CompilationResults.DefinitionCompilationResult(
            this,
            errors,
            false)

        compilationResults.definitionCompilationResults.put(this, localResult)
        return errors.isEmpty()
    }

    fun serializeSummary(writer: CodeWriter)
    fun serializeTitle(writer: CodeWriter)

    enum class Kind {
        FIELD, STATIC, FUNCTION, METHOD, TRAIT, STRUCT, UNIT, IMPL, UNPARSEABLE
    }
}