package com.sevenreup.fhir.core.tests.operations

import com.sevenreup.fhir.core.tests.Operation
import com.sevenreup.fhir.core.models.TestStatus
import com.sevenreup.fhir.core.tests.inputs.PathResult
import com.sevenreup.fhir.core.tests.inputs.PathResultType
import com.sevenreup.fhir.core.tests.inputs.TestTypes

class EqualsTo : Operation {
    override fun execute(pathResult: PathResult?, expected: Any?): TestStatus {
        val value = pathResult?.value
        if (expected != null && pathResult != null) {
            if (pathResult.type == PathResultType.ARRAY) {
                for (item in value as Iterable<*>) {
                    val passed = expected == item

                    if (passed) {
                        return TestStatus(
                            passed = true, value = value, expected = expected, exception = null
                        )
                    }
                }
                val error: Exception =
                    Exception("Expected: $expected but could not find in [${value.joinToString(",")}]")

                return TestStatus(
                    passed = false, value = value, expected = expected, exception = error
                )
            }
        }

        return evaluateSingle(value, expected)
    }

    private fun evaluateSingle(value: Any?, expected: Any?): TestStatus {
        val passed = expected == value
        var error: Exception? = null
        if (!passed) {
            error = Exception("Expected: $expected but got $value instead")
        }

        return TestStatus(
            passed = passed, value = value, expected = expected, exception = error
        )
    }

    companion object {
        const val Name = TestTypes.Equals
    }
}