package org.kobjects.tantilla2.core.definition

import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.CodeWriter
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

    val docString: String
        get() = ""

    var index: Int
        get() = -1
        set(_) = throw UnsupportedOperationException()

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

    /*
    fun resolveAll(): Boolean {
        try {
            resolve()
            userRootScope().definitionsWithErrors.remove(this)
            return true
        } catch (e: Exception) {
            userRootScope().definitionsWithErrors[this] = listOf(e)
            return false
        }
    }*/

    /**
     * Reset the compilation state
     */
    fun invalidate() {
        userRootScope().unresolved.add(this)
    }

    /**
     * Fully resolve this definition. Called from resolveAll().
     */
    fun resolve(applyOffset: Boolean = false, errorCollector: MutableList<ParsingException>? = null) {
    }

    fun isSummaryExpandable(): Boolean

    fun serializeSummary(writer: CodeWriter, kind: SummaryKind)

    enum class Kind {
        IMPORT, STATIC, FUNCTION, ENUM, ENUM_LITERAL, PROPERTY, METHOD, TRAIT, TYPE, STRUCT, UNIT, IMPL, UNPARSEABLE,
    }

    fun serializeQualifiedName(writer: CodeWriter) {
        // Am I local or at root?
        if (writer.forTitle
            || parentScope == null
            || parentScope == writer.scope
            || parentScope == AbsoluteRootScope
            || parentScope is UserRootScope
            || parentScope is SystemRootScope) {
            writer.append(name)
        } else {
            // Check for imports
            var scope: Scope? = writer.scope
            var found = false
            while (scope != null && !found) {
                for (definition in scope) {
                    if (definition is ImportDefinition && definition.getValue(null) == this) {
                        writer.append(definition.name)
                        found = true
                        break;
                    }
                }
                scope = scope.parentScope
            }

            if (!found) {
                parentScope!!.serializeQualifiedName(writer)
                writer.append('.')
                writer.append(name)
            }
        }

        if (this is Type) {
            serializeGenerics(writer)
        }
    }

    fun qualifiedName(raw: Boolean = false, relativeTo: Scope = AbsoluteRootScope): String {
        val writer = CodeWriter(scope = relativeTo)
        serializeQualifiedName(writer)
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
        var current = this
        while (current !is UserRootScope) {
            current = current?.parentScope ?: throw RuntimeException("User root scope not found for $name.")
        }
        return current
    }

    enum class SummaryKind {
        NESTED, COLLAPSED, EXPANDED
    }

}