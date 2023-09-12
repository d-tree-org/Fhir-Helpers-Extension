package com.sevenreup.fhir.cli.commands


import com.github.ajalt.mordant.rendering.TextColors.green
import com.github.ajalt.mordant.rendering.TextColors.red
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.terminal.Terminal
import com.sevenreup.fhir.core.compiler.parsing.ParseJsonCommands
import com.sevenreup.fhir.core.config.ProjectConfigManager
import com.sevenreup.fhir.core.tests.MapTestResult
import com.sevenreup.fhir.core.tests.StructureMapTests
import com.sevenreup.fhir.core.tests.TestResult
import com.sevenreup.fhir.core.tests.TestStatus
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

fun runTests(path: String, watch: Boolean, projectRoot: String?) {
    val configManager = ProjectConfigManager()
    val tests = StructureMapTests(configManager, ParseJsonCommands())
    if (watch) {
        runBlocking {
            tests.watchTestChanges(path, projectRoot).collect {
                printResults(it)
            }
        }
    } else {
        val results = tests.runTests(path, projectRoot)
        printResults(results)

        if (results.failed > 0) {
            exitProcess(1)
        }
    }
}

private fun printResults(result: TestResult) {
    val t = Terminal()
    val errStyle = (bold + red)
    val passStyle = (bold + green)
    var allPassedTests = 0
    var allFailedTests = 0
    for (map in result.list) {
        allPassedTests += map.allPassedTests
        allFailedTests += map.allFailedTests
        printMapResults(map, errStyle, passStyle)
    }

    if (result.list.size > 1) {
        t.println(errStyle("$DIVIDER$DIVIDER"))
        t.println("\nRunning Completed")
        if (allFailedTests > 0) {
            t.println("${errStyle.bg(" Failed ")} Some tests passed")
        } else {
            t.println("${passStyle.bg(" Passed ")} All tests passed")
        }
        t.println(
            "\nTest files ${
                createPassedFailedString(
                    result.passed,
                    result.failed,
                    passStyle,
                    errStyle
                )
            } (${result.files})"
        )
        t.println(
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
}

fun printMapResults(result: MapTestResult, errStyle: TextStyle, passStyle: TextStyle) {

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

private fun createPassedFailedString(pass: Int, fail: Int, passStyle: TextStyle, errStyle: TextStyle): String {
    val passed: String? = if (pass > 0) passStyle("$pass passed") else null
    val failed: String? = if (fail > 0) errStyle("$fail failed") else null
    return "${failed ?: ""}${if (failed != null && passed != null) " | " else ""}${passed ?: ""}"
}