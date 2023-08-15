package com.sevenreup.fhir.cli.commands

import com.github.ajalt.mordant.AnsiCode
import com.github.ajalt.mordant.TermColors
import com.sevenreup.fhir.core.compiler.parsing.ParseJsonCommands
import com.sevenreup.fhir.core.config.ProjectConfigManager
import com.sevenreup.fhir.core.tests.StructureMapTests
import com.sevenreup.fhir.core.tests.TestStatus
import com.sevenreup.fhir.core.utils.asWatchChannel
import com.sevenreup.fhir.core.utils.toAbsolutePath
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

fun runTests(path: String, watch: Boolean, projectRoot: String?) {
    val configManager = ProjectConfigManager()
    if (watch) {
        val file = File(path.toAbsolutePath())
        runBlocking {
            val channel = file.asWatchChannel()

            launch {
                channel.consumeEach { event ->
                    try {
                        runTestOnFiles(configManager, path, projectRoot)
                    } catch (e: Exception) {
                        val t = TermColors()
                        val errStyle = (t.bold + t.red)
                        println(errStyle(e.toString()))
                    }
                }
            }
        }
    } else {
        runTestOnFiles(configManager, path, projectRoot)
    }
}

private fun runTestOnFiles(configManager: ProjectConfigManager, path: String, projectRoot: String?) {
    val tests = StructureMapTests(configManager, ParseJsonCommands())
    val t = TermColors()
    val errStyle = (t.bold + t.red)
    val passStyle = (t.bold + t.green)

    val result = tests.test(path, projectRoot)
    var allPassedTests = 0
    var allFailedTests = 0
    val failedTests = mutableListOf<TestStatus>()
    print("\n")
    result.fileResults.forEach { testFile ->
        val hasFailed = testFile.failed > 0
        allPassedTests += testFile.passed
        allFailedTests += testFile.failed

        println("${if (!hasFailed) "\u2705 ${passStyle("Passed")}" else "\u274C ${errStyle("Failed")}"} ${testFile.file} (${testFile.tests})")
        failedTests.addAll(testFile.testResults.filter { !it.passed })
    }
    if (failedTests.isNotEmpty()) {
        failedTests.forEachIndexed { i, testStatus ->
            if (!testStatus.passed) {
                val index = i + 1
                println(errStyle("$DIVIDER Failed Tests $index $DIVIDER"))
                println(errStyle("Error: ${testStatus.exception?.message}"))
                println("\tJSONPath: ${testStatus.path}")
                println(errStyle("$DIVIDER [${index}/${failedTests.size}] $DIVIDER"))
            }
        }
    }


    println(
        "\nTest files ${
            createPassedFailedString(
                result.passed,
                result.failed,
                passStyle,
                errStyle
            )
        } (${result.files})"
    )
    println(
        "\tTests ${
            createPassedFailedString(
                allPassedTests,
                allFailedTests,
                passStyle,
                errStyle
            )
        } (${allFailedTests + allPassedTests})"
    )

}

private const val DIVIDER = "⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"

private fun createPassedFailedString(pass: Int, fail: Int, passStyle: AnsiCode, errStyle: AnsiCode): String {
    val passed: String? = if (pass > 0) passStyle("$pass passed") else null
    val failed: String? = if (fail > 0) errStyle("$fail failed") else null
    return "${failed ?: ""}${if (failed != null && passed != null) " | " else ""}${passed ?: ""}"
}