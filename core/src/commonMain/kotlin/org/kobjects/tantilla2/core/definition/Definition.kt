package org.kobjects.tantilla2.core.definition

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.CompilationResults
import org.kobjects.tantilla2.core.SerializableCode
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.dynamicType

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

    fun getValue(self: Any?): Any

    fun setValue(self: Any?, newValue: Any): Unit = throw UnsupportedOperationException()

    fun depth(scope: Scope): Int {
        if (scope == this.parentScope) {
            return 0
        }
        if (scope.parentScope == null) {
            throw IllegalStateException("Definition $this not found in scope.")
        }
        return 1 + depth(scope.parentScope!!)
    }

    fun findNode(node: Node): Definition? = null
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

    fun createWriter(): CodeWriter {
        return CodeWriter("", )
    }

    fun isSummaryExpandable(): Boolean

    fun serializeSummary(writer: CodeWriter, kind: SummaryKind)

    enum class Kind {
        IMPORT, STATIC, FUNCTION, ENUM, ENUM_LITERAL, PROPERTY, METHOD, TRAIT, TYPE, STRUCT, UNIT, IMPL, UNPARSEABLE,
    }

    fun serializeQualifiedName(writer: CodeWriter, raw: Boolean) {
        // Am I local or at root?
        if (writer.forTitle
            || parentScope == writer.scope
            || parentScope == null
            || parentScope == AbsoluteRootScope
            || parentScope is UserRootScope
            || parentScope is SystemRootScope) {
            if (raw) {
                writer.append("::")
            }
            writer.append(name)
        } else {

            // Check for imports
            var scope: Scope? = writer.scope
            var found = false
            while (scope != null && !found) {
                for (definition in scope) {
                    if (definition is ImportDefinition && definition.getValue(null) == this) {
                        if (raw) {
                            writer.append("::")
                        }
                        writer.append(definition.name)
                        found = true
                        break;
                    }
                }
                scope = scope.parentScope
            }

            if (!found) {
                parentScope!!.serializeQualifiedName(writer, false)
                writer.append(if (raw) "::" else ".")
                writer.append(name)
            }
        }

        if (this is Type) {
            serializeGenerics(writer)
        }
    }

    fun qualifiedName(raw: Boolean = false, relativeTo: Scope = AbsoluteRootScope): String {
        val writer = CodeWriter(scope = relativeTo)
        serializeQualifiedName(writer, raw)
        return writer.toString()
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

    enum class SummaryKind {
        NESTED, COLLAPSED, EXPANDED
    }

}