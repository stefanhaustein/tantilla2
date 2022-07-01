package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable

interface Definition : SerializableCode {
    val scope: Scope?
    val kind: Kind
    val name: String
    val mutable: Boolean

    var docString: String
    var index: Int

    fun value(): Any?
    fun type(): Type
    fun error(): Exception?
    fun initializer(): Evaluable<RuntimeContext>?
    fun depth(scope: Scope): Int

    fun findNode(node: Evaluable<RuntimeContext>): DefinitionImpl?
    fun isDynamic(): Boolean
    fun isScope(): Boolean
    fun rebuild(compilationResults: CompilationResults): Boolean

    fun serializeSummaray(writer: CodeWriter)
    fun serializeTitle(writer: CodeWriter)

    enum class Kind {
        FIELD, STATIC, FUNCTION, METHOD, TRAIT, STRUCT, UNIT, IMPL, UNPARSEABLE
    }
}