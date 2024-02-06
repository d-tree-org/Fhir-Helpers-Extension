package com.sevenreup.fhir.core.tests.inputs

import com.sevenreup.fhir.core.models.DefaultTests


data class TestCaseData @JvmOverloads constructor(
    val range: Range = Range(),
    val response: String = "",
    val path: String = "",
    val value: String? = null,
    val valueRange: ValueRange? = null,
    val type: String = "",
    val defaultTests: List<DefaultTests> = listOf()
)

data class Range @JvmOverloads constructor(
    val start: Position = Position(),
    val end: Position = Position()
)

data class Position @JvmOverloads constructor(
    val line: Int = 0,
    val character: Int = 0
)

data class TestCaseRaw(val path: String, val data: TestCaseData)