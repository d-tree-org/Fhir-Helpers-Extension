package com.sevenreup.fhir.core.tests.operations

import com.sevenreup.fhir.core.tests.Operation
import com.sevenreup.fhir.core.tests.TestStatus
import com.sevenreup.fhir.core.tests.TestTypes

class EqualsTo : Operation {
    override fun execute(value: Any, expected: Any): TestStatus {
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