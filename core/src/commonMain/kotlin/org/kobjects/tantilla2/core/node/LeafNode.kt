package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.Evaluable

abstract class LeafNode : Node() {

    override fun children(): List<Node> = emptyList()

    override fun reconstruct(newChildren: List<Node>): Evaluable = this

}