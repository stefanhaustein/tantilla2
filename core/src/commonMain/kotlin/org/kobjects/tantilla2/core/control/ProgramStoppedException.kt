package org.kobjects.tantilla2.core.control

import org.kobjects.tantilla2.core.Evaluable

class ProgramStoppedException(node: Evaluable) : TantillaRuntimeException(null, node, "Program Stopped")