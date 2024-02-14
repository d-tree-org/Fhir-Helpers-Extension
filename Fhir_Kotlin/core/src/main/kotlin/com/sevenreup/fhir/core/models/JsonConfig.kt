package com.sevenreup.fhir.core.models

import com.sevenreup.fhir.core.tests.inputs.TestCaseData
import com.sevenreup.fhir.core.tests.inputs.ValueRange
import com.sevenreup.fhir.core.tests.inputs.ValueTypes
import kotlinx.serialization.Serializable

@Serializable
data class JsonConfig(
    val type: String = "", val map: MapConfig = MapConfig(), val tests: List<ResTest> = listOf(),
)

@Serializable
data class ResTest(
    val response: String, val verify: List<TestVerify>, val title: String? = ""
)

@Serializable
data class TestVerify(
    val type: String = "",
    val path: String = "",
    val title: String? = "",
    val value: String? = null,
    val valueRange: ValueRange? = null,
    val valueStringArray: List<String>? = null,
) {
    fun actualValue(type: ValueTypes): Any? {
        return when (type) {
            ValueTypes.Array -> valueStringArray
            ValueTypes.Range -> valueRange
            else -> value
        }
    }

    companion object {
        fun fromTestCaseData(data: TestCaseData): TestVerify {
            return TestVerify(
                type = data.type,
                path = data.path,
                value = data.value,
                valueRange = data.valueRange,
                valueStringArray = data.valueStringArray
            )
        }
    }
}