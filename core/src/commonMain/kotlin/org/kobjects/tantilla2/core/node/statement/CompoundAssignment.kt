package org.kobjects.tantilla2.core.node.statement

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.*
import kotlin.math.pow

class CompoundAssignment(
    val name: String,
    val target: Node,
    val source: Node,
    val operation: (Any, Any) -> Any
) : Node() {

    override val returnType: Type
        get() = NoneType

    override fun children() = listOf(target, source)

    override fun eval(ctx: LocalRuntimeContext) =
        target.assign(ctx, operation(target.eval(ctx), source.eval(ctx)))

    override fun reconstruct(newChildren: List<Node>) =
        CompoundAssignment(name, newChildren[0] , newChildren[1], operation)

    override fun serializeCode(sb: CodeWriter, parentPrecedence: Int) {
        sb.appendCode(target)
        sb.append(" ").append(name)
        sb.appendMaybeNextLine(source)
    }

    companion object {
        fun create(name: String, target: Node, source: Node): CompoundAssignment {
            val op: (Any, Any) -> Any = when (target.returnType) {
                IntType -> {
                    if (source.returnType != IntType) {
                        throw IllegalArgumentException("Int operand required for compound assignment to an Int value.")
                    }
                    when (name) {
                        "+=" -> { a, b -> (a as Long) + (b as Long) }
                        "-=" -> { a, b -> (a as Long) - (b as Long) }
                        "*=" -> { a, b -> (a as Long) * (b as Long) }
                        "%=" -> { a, b -> (a as Long) % (b as Long) }
                        "//=" -> { a, b -> (a as Long) / (b as Long) }
                        "&=" -> { a, b -> (a as Long) and (b as Long) }
                        "|=" -> { a, b -> (a as Long) or (b as Long) }
                        "^=" -> { a, b -> (a as Long) xor (b as Long) }
                        ">>=" -> { a, b -> (a as Long) shr (b as Long).toInt() }
                        "<<=" -> { a, b -> (a as Long) shl (b as Long).toInt() }
                        else -> throw IllegalArgumentException("Operator $name not supported for Int target.")
                    }
                }
                FloatType -> when (name) {
                    "+=" -> { a, b -> (a as Double) + (b as Number).toDouble() }
                    "-=" -> { a, b -> (a as Double) - (b as Number).toDouble() }
                    "*=" -> { a, b -> (a as Double) * (b as Number).toDouble() }
                    "%=" -> { a, b -> (a as Double) % (b as Number).toDouble() }
                    "/=" -> { a, b -> (a as Double) / (b as Number).toDouble() }
                    "**=" -> { a, b -> (a as Double).pow((b as Number).toDouble()) }
                    else -> throw IllegalArgumentException("Operator $name not supported for Float target.")
                }
                StrType -> {
                    if (name != "+=") {
                        throw IllegalArgumentException("Operator $name not supported for a Str target.")
                    }
                    { a, b -> (a as String) + (b as String) }
                }
                else -> throw IllegalArgumentException("Compound assignment not supported for target type ${target.returnType}")
            }
            return CompoundAssignment(name, target, source, op)
        }
    }

    init {
        target.requireAssignability()
    }
}