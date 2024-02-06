package com.sevenreup.fhir.core.models

data class ResponseTestResult(
    val file: String,
    val tests: Int,
    val passed: Int,
    val failed: Int,
    val testResults: List<TestStatus>,
    val defaultTestsResults: List<DefaultTestResult> = listOf()
)

data class TestStatusData @JvmOverloads constructor(
    val status: TestStatus? = null,
    val defaultTestsResults: List<DefaultTestResult> = listOf()
)

data class TestStatus @JvmOverloads constructor(
    val passed: Boolean,
    val value: Any? = null,
    val expected: Any? = null,
    val exception: Exception? = null,
    val path: String? = null
)

data class MapTestResult(
    val fileResults: List<ResponseTestResult>,
    val failed: Int,
    val passed: Int,
    val files: Int,
    val allPassedTests: Int,
    val allFailedTests: Int
)


data class TestResult(
    val list: List<MapTestResult>, val failed: Int, val passed: Int, val files: Int
)

data class DefaultTestResult(val passed: Boolean, val testResults: List<TestStatus>)