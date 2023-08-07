package com.sevenreup.fhir.core.models

import com.sevenreup.fhir.core.tests.inputs.ValueRange
import kotlinx.serialization.Serializable

@Serializable
data class JsonConfig(
    val type: String = "", val map: MapConfig = MapConfig(), val tests: List<ResTest> = listOf()
)

@Serializable
data class ResTest(
    val response: String, val verify: List<TestVerify>
)

@Serializable
data class TestVerify(
    val type: String = "", val path: String = "", val value: String? = null, val valueRange: ValueRange? = null
)