package com.sevenreup.fhir.compiler.utils

import org.apache.commons.io.FileUtils
import kotlin.io.path.Path

fun String.readFile(): String {
    return FileUtils.readFileToString(FileUtils.getFile(this), "UTF-8")
}

fun String.getAbsolutePath(parent: String): String {
    val childPath = Path(this)
    var effectivePath = childPath
    if (!childPath.isAbsolute) {
        effectivePath = Path(parent).resolve(this).toAbsolutePath()
    }
    return effectivePath.normalize().toString()
}

fun String.getParentPath(): String {
    var cP = Path(this)
    if (!cP.isAbsolute) {
        cP = cP.toAbsolutePath()
    }
    return cP.normalize().parent.toString()
}