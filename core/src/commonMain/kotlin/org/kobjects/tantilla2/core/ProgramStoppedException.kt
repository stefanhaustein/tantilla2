package org.kobjects.tantilla2.core

class ProgramStoppedException(node: Evaluable) : TantillaRuntimeException(null, node, "Program Stopped")