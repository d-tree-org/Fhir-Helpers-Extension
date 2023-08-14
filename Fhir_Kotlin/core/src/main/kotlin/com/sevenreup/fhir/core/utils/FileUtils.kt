package com.sevenreup.fhir.core.utils

import org.apache.commons.io.FileUtils
import kotlin.io.path.Path
import kotlin.io.path.isDirectory

fun String.readFile(): String {
    return FileUtils.readFileToString(FileUtils.getFile(this), "UTF-8")
}

fun String.readFileOrNull(): String? {
    return try {
        this.readFile()
    } catch (e: Exception) {
        null
    }
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

fun String.getActualPath(): String {
    val cp = Path(this)
    if (cp.isDirectory()) {
        return this
    }
    return this.getParentPath()
}

fun String.toAbsolutePath(): String {
    var cP = Path(this)
    if (!cP.isAbsolute) {
        cP = cP.toAbsolutePath()
    }
    return cP.toString()
}