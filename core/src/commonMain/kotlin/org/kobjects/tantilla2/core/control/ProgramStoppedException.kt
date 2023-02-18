package org.kobjects.tantilla2.core.control

import org.kobjects.tantilla2.core.node.Node


class ProgramStoppedException(node: Node) : TantillaRuntimeException(null, node, "Program Stopped")