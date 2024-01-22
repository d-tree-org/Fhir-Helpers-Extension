package com.sevenreup.fhir.core.tests.reports

import com.sevenreup.fhir.core.config.ProjectConfig
import com.sevenreup.fhir.core.models.TestResult
import com.sevenreup.fhir.core.models.TestStatus
import com.sevenreup.fhir.core.utils.createFile

private const val DIVIDER = "⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"

class MarkdownResults {
    private val stringBuilder: StringBuilder = StringBuilder()

    fun save(config: ProjectConfig) {
        stringBuilder.toString().createFile("${config.reportPath}/report.md")
    }

    fun createHeader(result: TestResult, allPassedTests: Int, allFailedTests: Int) {
        if (allFailedTests > 0) {
            stringBuilder.appendLine("\n${errStyle(" Failed ")} Some tests passed\n")
        } else {
            stringBuilder.appendLine("\n${passStyle(" Passed ")} All tests passed\n")
        }
        stringBuilder.appendLine(
            "\nTests ${
                createPassedFailedString(
                    allPassedTests,
                    allFailedTests
                )
            } (${allFailedTests + allPassedTests})\n"
        )
    }

    fun printMapResults(failedTest: Map<String, List<TestStatus>>) {
        if (failedTest.isNotEmpty()) {
            stringBuilder.appendLine("\n## Failed Tests\n\n")
            failedTest.forEach { (key, list) ->
                stringBuilder.appendLine("### File: ${key}\n")
                list.forEachIndexed { idx, testStatus ->
                    val index = idx + 1
                    stringBuilder.appendLine("$DIVIDER Failed Tests $index $DIVIDER\n")
                    stringBuilder.appendLine("``Error: ${testStatus.exception?.message}``\n")
                    stringBuilder.appendLine("\tJSONPath: ${testStatus.path}\n")
                    stringBuilder.appendLine("$DIVIDER [${index}/${list.size}] $DIVIDER")
                }
            }
        }
    }
}

private fun errStyle(value: String): String {
    return "<span style=\"color:green\">${value}</span>"
}

private fun passStyle(value: String): String {
    return "<span style=\"color:red\">${value}</span>"
}

private fun createPassedFailedString(pass: Int, fail: Int): String {
    val passed: String? = if (pass > 0) passStyle("$pass passed") else null
    val failed: String? = if (fail > 0) errStyle("$fail failed") else null
    return "${failed ?: ""}${if (failed != null && passed != null) " | " else ""}${passed ?: ""}"
}