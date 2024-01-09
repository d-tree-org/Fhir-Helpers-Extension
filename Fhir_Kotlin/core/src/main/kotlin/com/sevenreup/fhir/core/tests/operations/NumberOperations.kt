package com.sevenreup.fhir.core.tests.operations

import com.sevenreup.fhir.core.tests.Operation
import com.sevenreup.fhir.core.models.TestStatus
import com.sevenreup.fhir.core.tests.inputs.PathResult
import com.sevenreup.fhir.core.tests.inputs.testTypeNameMap
import java.math.BigDecimal

open class NumberOperations : Operation {

    open fun getName(): String {
        TODO("Implement this in the class that inherits")
    }

    open fun calculateValue(value: BigDecimal, expected: BigDecimal): Boolean {
        TODO("Implement this in the class that inherits")
    }

    override fun execute(value: PathResult?, expected: Any?): TestStatus {
        return if (value?.value is String && expected is String) {
            val expectedNumber = expected.toBigDecimalOrNull()
            val valueNumber = value.toString().toBigDecimalOrNull()
            val passed: Boolean
            var error: Exception? = null
            if (expectedNumber == null || valueNumber == null) {
                passed = false
                error = Exception("Expected number got {value: $value, expected: $expected")
            } else {
                passed = calculateValue(valueNumber, expectedNumber)
                if (!passed)
                    error = Exception("Expected: $value ${testTypeNameMap[getName()]} $expected")
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
}