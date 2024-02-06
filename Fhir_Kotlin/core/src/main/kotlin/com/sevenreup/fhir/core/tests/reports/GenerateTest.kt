package com.sevenreup.fhir.core.tests.reports

import com.google.gson.GsonBuilder
import com.sevenreup.fhir.core.config.ProjectConfig
import com.sevenreup.fhir.core.models.TestResult
import com.sevenreup.fhir.core.utilities.ThrowableTypeAdapter
import com.sevenreup.fhir.core.utils.createFile
import com.sevenreup.fhir.core.utils.verifyDirectories

fun generateTestReport(result: TestResult, config: ProjectConfig) {
    val gson = GsonBuilder()
        .registerTypeAdapter(Exception::class.java, ThrowableTypeAdapter())
        .create()
    config.reportPath.verifyDirectories()
    val content = gson.toJson(MochaResults.fromTestResult(result))
    content.createFile("${config.reportPath}/report.json")
}