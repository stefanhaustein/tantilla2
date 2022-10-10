package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable

class ProgramStoppedException(node: Evaluable<LocalRuntimeContext>) : TantillaRuntimeException(null, node, "Program Stopped")