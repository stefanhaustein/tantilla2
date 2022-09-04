package org.kobjects.tantilla2.core

class GlobalRuntimeContext(
    // Call with null to clear errors.
    val endCallback: (TantillaRuntimeException?) -> Unit
) {
    var stopRequested = false
    var activeThreads = 0
}