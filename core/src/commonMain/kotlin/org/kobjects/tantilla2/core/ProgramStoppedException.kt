package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.node.Evaluable

class ProgramStoppedException(node: Evaluable) : TantillaRuntimeException(null, node, "Program Stopped")