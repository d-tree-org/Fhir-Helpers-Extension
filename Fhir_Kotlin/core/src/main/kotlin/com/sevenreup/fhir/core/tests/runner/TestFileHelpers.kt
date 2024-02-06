package com.sevenreup.fhir.core.tests.runner

import com.charleskorn.kaml.Yaml
import com.google.gson.Gson
import com.sevenreup.fhir.core.models.JsonConfig
import com.sevenreup.fhir.core.utils.readFile
import java.io.File

object TestFileHelpers {
    fun getAllTestFiles(directoryPath: String): List<String> {
        val extensions = listOf(".map.test.yaml", ".map.test.json", ".map.test.yml")

        val fileList = mutableListOf<File>()
        findFilesWithExtension(File(directoryPath), extensions, fileList)

        return fileList.map { it.absolutePath }
    }

    fun readTestFile(path: String): JsonConfig {
        return when (path.split(".").last()) {
            "json" -> {
                val rawJson = path.readFile()
                val gson = Gson()
                gson.fromJson(rawJson, JsonConfig::class.java)
            }

            "yml", "yaml" -> {
                val rawJson = path.readFile()
                Yaml.default.decodeFromString(JsonConfig.serializer(), rawJson)
            }

            else -> {
                throw Exception("File format not supported")
            }
        }
    }

    private fun findFilesWithExtension(directory: File, extensions: List<String>, fileList: MutableList<File>) {
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
}

