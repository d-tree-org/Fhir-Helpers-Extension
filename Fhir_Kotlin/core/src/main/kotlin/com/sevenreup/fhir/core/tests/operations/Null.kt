package com.sevenreup.fhir.core.tests.operations

import com.sevenreup.fhir.core.tests.Operation
import com.sevenreup.fhir.core.tests.TestStatus
import com.sevenreup.fhir.core.tests.inputs.TestTypes

class Null : Operation {
    override fun execute(value: Any?, expected: Any?): TestStatus {
        val passed = value == null
        var error: Exception? = null
        if (!passed) {
            error = Exception("Null Expected")
        }

        return TestStatus(
            passed = passed, value = value, expected = expected, exception = error
        )
    }

    companion object {
        const val Name = TestTypes.Null
    }
}