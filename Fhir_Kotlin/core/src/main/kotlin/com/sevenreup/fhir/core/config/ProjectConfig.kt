package com.sevenreup.fhir.core.config

import com.google.gson.Gson
import com.sevenreup.fhir.core.utils.*
import java.nio.file.Paths

class ProjectConfigManager {
    fun loadProjectConfig(projectRoot: String?, file: String?): ProjectConfig {
        println("Project Root: $projectRoot, $file")
        if (projectRoot != null) {
            return searchForConfig(projectRoot.toAbsolutePath())
        } else {
            if (file != null) {
                val parentPath = file.getParentPath()
                return searchForConfig(parentPath)
            }
        }
        return ProjectConfig()
    }

    fun loadUploaderConfigs(projectConfig: ProjectConfig,projectRoot: String): AppConfig {
        val configs = projectConfig.appConfigs.getAbsolutePath(projectRoot).readFile()
        val gson = Gson()
        return gson.fromJson(configs, AppConfig::class.java)
    }

    private fun searchForConfig(path: String): ProjectConfig {
        val actualPath = Paths.get(path, "fhir.compiler.json").normalize().toString()
        println("$path - $actualPath")
        val gson = Gson()
        val rawJson: String = actualPath.readFileOrNull() ?: return ProjectConfig()
        return gson.fromJson(rawJson, ProjectConfig::class.java).copy(basePath = path)
    }
}


data class ProjectConfig(
    val aliases: Map<String, String> = mapOf(),
    val basePath: String? = null,
    val compileMode: CompileMode = CompileMode.Silent,
    val generateReport: Boolean = false,
    val generateReportMarkdown: Boolean = false,
    val reportPath: String = "./reports",
    val structureMapLocation: String = "structure_map",
    val questionnaireMapLocation: String = "questionnaire",
    val uploadExclude: List<String> = listOf(),
    val appConfigs: String = "./app_config/uploader.json",
    val cacheConfig: Boolean = false
)


enum class CompileMode {
    Debug,
    Production,
    Silent
}