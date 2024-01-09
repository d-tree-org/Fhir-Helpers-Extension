package com.sevenreup.fhir.core.models

data class MochaResults(
    val stats: MochaStats,
    val tests: List<MochaTest>,
    val pending: List<MochaTest>,
    val failures: List<MochaTest>,
    val passes: List<MochaTest>,
) {
    companion object {
        fun fromTestResult(results: TestResult): MochaResults {
            val tests: MutableList<MochaTest> = mutableListOf()
            val pending: MutableList<MochaTest> = mutableListOf()
            val failures: MutableList<MochaTest> = mutableListOf()
            val passes: MutableList<MochaTest> = mutableListOf()
            var allTests = 0
            results.list.forEach { result ->
                result.fileResults.forEach { file ->
                    file.testResults.forEach { test ->
                        allTests++
                        val mocha = MochaTest(
                            title = test.path ?: "",
                            fullTitle = test.path ?: "",
                            file = file.file,
                            duration = 0,
                            currentRetry = 0,
                            speed = 0,
                            err = test.exception
                        )
                        tests.add(mocha)
                        if (test.passed) {
                            passes.add(mocha)
                        } else {
                            failures.add(mocha)
                        }
                    }
                }
            }

            return MochaResults(
                MochaStats(
                    suites = results.files,
                    tests = allTests,
                    passes = results.passed,
                    pending = 0,
                    failures = results.failed,
                ), tests, pending, failures, passes
            )
        }
    }
}

data class MochaStats(
    val suites: Int = 0,
    val tests: Int = 0,
    val passes: Int = 0,
    val pending: Int = 0,
    val failures: Int = 0
)

data class MochaTest(
    val title: String,
    val fullTitle: String,
    val file: String,
    val duration: Int,
    val currentRetry: Int,
    val speed: Int,
    val  err: Exception?,
)