package org.kobjects.tantilla2.core.parser

import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.node.expression.FloatNode
import org.kobjects.tantilla2.core.node.expression.IntNode
import org.kobjects.tantilla2.core.node.expression.StrNode
import org.kobjects.tantilla2.core.type.FloatType
import org.kobjects.tantilla2.core.type.IntType
import org.kobjects.tantilla2.core.type.StrType

object NodeFactory {


    fun add(l: Node, r: Node) =
        if (l.returnType == StrType) StrNode.Add(l, r)
        else if (bothInt(l, r)) IntNode.Add(l, r)
        else FloatNode.Add(l, r)

    fun bothNumber(l: Node, r: Node) =
        FloatType.isAssignableFrom(l.returnType)
                && FloatType.isAssignableFrom(r.returnType)

    fun bothInt(l: Node, r: Node) =
        l.returnType == org.kobjects.tantilla2.core.type.IntType
                && r.returnType == org.kobjects.tantilla2.core.type.IntType

    fun eq(l: Node, r: Node) =
        if (bothInt(l, r)) IntNode.Eq(l, r)
        else if (bothNumber(l, r)) FloatNode.Eq(l, r)
        else StrNode.Eq(l, r)

    fun mod(l: Node, r: Node) =
        if (bothInt(l, r)) IntNode.Mod(l, r) else FloatNode.Mod(l, r)

    fun mul(l: Node, r: Node) =
        if (bothInt(l, r)) IntNode.Mul(l, r) else FloatNode.Mul(l, r)


    fun ne(l: Node, r: Node) =
        if (bothInt(l, r)) IntNode.Ne(l, r)
        else  if (bothNumber(l, r)) FloatNode.Ne(l, r)
        else StrNode.Ne(l, r)

    fun neg(expr: Node) =
        if (expr.returnType == IntType) IntNode.Neg(expr)
        else FloatNode.Neg(expr)

    fun sub(l: Node, r: Node) =
        if (bothInt(l, r)) IntNode.Sub(l, r) else FloatNode.Sub(l, r)


    fun lt(l: Node, r: Node) =
        if (bothInt(l, r)) IntNode.Lt(l, r) else FloatNode.Lt(l, r)

    fun gt(l: Node, r: Node) =
        if (bothInt(l, r)) IntNode.Gt(l, r) else FloatNode.Gt(l, r)

    fun ge(l: Node, r: Node) =
        if (bothInt(l, r)) IntNode.Ge(l, r) else FloatNode.Ge(l, r)

    fun le(l: Node, r: Node) =
        if (bothInt(l, r)) IntNode.Le(l, r) else FloatNode.Le(l, r)



}