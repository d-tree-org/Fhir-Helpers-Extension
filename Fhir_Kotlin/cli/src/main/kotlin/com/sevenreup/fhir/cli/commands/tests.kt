package com.sevenreup.fhir.cli.commands

import com.sevenreup.fhir.core.tests.StructureMapTests

fun runTests(path: String) {
    val result = StructureMapTests.test(path)
    var allPassedTests = 0
    var allFailedTests = 0

    result.fileResults.forEach { testFile ->
        val hasFailed = testFile.failed > 0
        allPassedTests += testFile.passed
        allFailedTests += testFile.failed
        println("${if (!hasFailed) "✅ Passed" else "❌ Failed"} ${testFile.file} (${testFile.tests})")
    }

    println("\nTest files ${createPassedFailedString(result.passed, result.failed)} (${result.files})")
    println("\tTests ${createPassedFailedString(allPassedTests, allFailedTests)} (${allFailedTests + allPassedTests})")
}

private fun createPassedFailedString(pass: Int, fail: Int): String {
    val passed: String? = if (pass > 0) "$pass passed" else null
    val failed: String? = if (fail > 0)"$fail failed" else null
    return "${failed ?: ""}${if (failed != null && passed != null) " | " else "" }${passed ?: ""}"
}