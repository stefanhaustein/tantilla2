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

    fun value(): Any?
    fun valueType(): Type
    fun error(): Exception?
    fun depth(scope: Scope): Int

    fun findNode(node: Evaluable<RuntimeContext>): Definition?
    fun isDynamic(): Boolean
    fun isScope(): Boolean
    fun rebuild(compilationResults: CompilationResults): Boolean

    fun serializeSummary(writer: CodeWriter)
    fun serializeTitle(writer: CodeWriter)

    enum class Kind {
        FIELD, STATIC, FUNCTION, METHOD, TRAIT, STRUCT, UNIT, IMPL, UNPARSEABLE
    }
}