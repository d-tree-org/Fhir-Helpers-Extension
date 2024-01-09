package com.sevenreup.fhir.core.tests.runner

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sevenreup.fhir.core.config.ProjectConfig
import com.sevenreup.fhir.core.tests.TestResult
import com.sevenreup.fhir.core.utilities.ThrowableTypeAdapter
import com.sevenreup.fhir.core.utils.createFile
import com.sevenreup.fhir.core.utils.verifyDirectories
import java.io.File

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

fun generateTestReport(result: TestResult, config: ProjectConfig) {
    val gson = GsonBuilder()
        .registerTypeAdapter(Exception::class.java, ThrowableTypeAdapter())
        .create()
    config.reportPath.verifyDirectories()
    val content = gson.toJson(result)
    content.createFile("${config.reportPath}/report.json")
}