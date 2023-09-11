package com.sevenreup.fhir.core.tests.operations

import com.sevenreup.fhir.core.tests.Operation
import com.sevenreup.fhir.core.tests.TestStatus
import com.sevenreup.fhir.core.tests.inputs.PathResult
import com.sevenreup.fhir.core.tests.inputs.PathResultType
import com.sevenreup.fhir.core.tests.inputs.TestTypes

class NotNull : Operation {
    override fun execute(value: PathResult?, expected: Any?): TestStatus {
        if (expected != null && value != null) {
            if (value.type == PathResultType.ARRAY) {
                for (item in value.value as Iterable<*>) {
                    val passed = item != null

                    if (passed) {
                        return TestStatus(
                            passed = true, value = value, expected = expected, exception = null
                        )
                    }
                }
                val error: Exception =
                    Exception("Expected: $expected but could not find in [${value.value.joinToString(",")}]")

                return TestStatus(
                    passed = false, value = value, expected = expected, exception = error
                )
            }
        }

        return evaluateSingle(value?.value, expected)
    }

    private fun evaluateSingle(value: Any?, expected: Any?): TestStatus {
        val passed = value != null
        var error: Exception? = null
        if (!passed) {
            error = Exception("Expected not null but found $value")
        }

        return TestStatus(
            passed = passed, value = value, expected = expected, exception = error
        )
    }

    companion object {
        const val Name = TestTypes.Null
    }
}