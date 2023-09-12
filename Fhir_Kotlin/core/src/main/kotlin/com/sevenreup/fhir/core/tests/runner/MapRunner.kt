package com.sevenreup.fhir.core.tests.runner

import java.io.File

class MapRunner {

}

fun getAllTestFiles(directoryPath: String): List<String> {
    val extension = ".map.test.yaml"

    val fileList = mutableListOf<File>()
    findFilesWithExtension(File(directoryPath), extension, fileList)

    return fileList.map { it.absolutePath }
}

fun findFilesWithExtension(directory: File, extension: String, fileList: MutableList<File>) {
    val files = directory.listFiles()
    if (files != null) {
        for (file in files) {
            if (file.isDirectory) {
                // Recursively search in subdirectories
                findFilesWithExtension(file, extension, fileList)
            } else if (file.isFile && file.name.endsWith(extension)) {
                fileList.add(file)
            }
        }
    }
}