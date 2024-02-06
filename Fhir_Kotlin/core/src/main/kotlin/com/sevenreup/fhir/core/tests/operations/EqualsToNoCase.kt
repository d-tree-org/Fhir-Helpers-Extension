package com.sevenreup.fhir.core.tests.operations

import com.sevenreup.fhir.core.models.TestStatus
import com.sevenreup.fhir.core.tests.inputs.PathResult
import com.sevenreup.fhir.core.tests.inputs.TestTypes

class EqualsToNoCase : Operation {
    override fun execute(value: PathResult?, expected: Any?): TestStatus {
        return if (value?.value is String) {
            val passed = value.toString().equals(expected.toString(), ignoreCase = true)
            var error: Exception? = null
            if (!passed) {
                error = Exception("Expected: $expected but got $value instead")
            }

            TestStatus(
                passed = passed, value = value, expected = expected, exception = error
            )
        } else {
            TestStatus(
                passed = false, value = value, expected = expected, exception = Exception("Expected String")
            )
        }
    }

    companion object {
        const val Name = TestTypes.Equals
    }
}