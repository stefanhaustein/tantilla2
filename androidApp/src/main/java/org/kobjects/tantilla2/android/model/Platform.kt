package org.kobjects.tantilla2.android.model

import org.kobjects.tantilla2.core.system.SystemAbstraction
import java.io.File

interface Platform : SystemAbstraction {
    val rootDirectory: File
    var fileName: String
    fun loadExample(name: String): String
}