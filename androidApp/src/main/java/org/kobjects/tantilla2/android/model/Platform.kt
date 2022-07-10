package org.kobjects.tantilla2.android.model

import java.io.File

interface Platform {
    val rootDirectory: File
    var fileName: String
    fun loadExample(name: String): String
}