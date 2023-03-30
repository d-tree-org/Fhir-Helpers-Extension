package com.sevenreup.fhir.core.tests.operations

import com.sevenreup.fhir.core.tests.Operation
import com.sevenreup.fhir.core.tests.TestStatus
import com.sevenreup.fhir.core.tests.TestTypes

class EqualsToNoCase : Operation {
    override fun execute(value: Any?, expected: Any?): TestStatus {
        return if (value is String) {
            val passed = value.equals(expected.toString(), ignoreCase = true)
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