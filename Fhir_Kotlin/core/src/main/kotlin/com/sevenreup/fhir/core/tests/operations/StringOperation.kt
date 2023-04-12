package com.sevenreup.fhir.core.tests.operations

import com.sevenreup.fhir.core.tests.Operation
import com.sevenreup.fhir.core.tests.TestStatus
import com.sevenreup.fhir.core.tests.inputs.testTypeNameMap

open class StringOperation : Operation {

    open fun getName(): String {
        TODO("Implement this in the class that inherits")
    }

    open fun calculateValue(value: String, expected: String): Boolean {
        TODO("Implement this in the class that inherits")
    }

    override fun execute(value: Any?, expected: Any?): TestStatus {
        return if (value is String && expected is String) {
            var error: Exception? = null
            val passed: Boolean = calculateValue(value, expected)
            if (!passed)
                error = Exception("Expected: $value ${testTypeNameMap[getName()]} $expected")
            TestStatus(
                passed = passed, value = value, expected = expected, exception = error
            )
        } else {
            TestStatus(
                passed = false, value = value, expected = expected, exception = Exception("Expected String")
            )
        }
    }
}