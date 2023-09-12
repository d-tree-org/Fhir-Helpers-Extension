package com.sevenreup.fhir.core.tests.runner

import java.io.File

class MapRunner {

}

fun getAllTestFiles(directoryPath: String): List<String> {
    val extensions = listOf(".map.test.yaml", ".map.test.json", ".map.test.yml")

    val fileList = mutableListOf<File>()
    findFilesWithExtension(File(directoryPath), extensions, fileList)

    return fileList.map { it.absolutePath }
}

fun findFilesWithExtension(directory: File, extensions: List<String>, fileList: MutableList<File>) {
    val files = directory.listFiles()

    if (files != null) {
        for (file in files) {
            if (file.isDirectory) {
                findFilesWithExtension(file, extensions, fileList)
            } else if (file.isFile && extensions.firstOrNull { file.name.endsWith(it) } != null) {
                fileList.add(file)
            }
        }
    }
}