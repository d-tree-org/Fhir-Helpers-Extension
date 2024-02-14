package com.sevenreup.fhir.core.tests.operations

import com.sevenreup.fhir.core.models.TestStatus
import com.sevenreup.fhir.core.tests.inputs.PathResult
import com.sevenreup.fhir.core.tests.inputs.PathResultType
import com.sevenreup.fhir.core.tests.inputs.TestTypes

class ArrayEquals : Operation {
    override fun execute(pathResult: PathResult?, expected: Any?): TestStatus {
        if (expected == null || pathResult == null) {
            return TestStatus(
                passed = false,
                value = "",
                expected = expected,
                exception = Exception("Expected or value cannot be null.")
            )
        }

        if (pathResult.type != PathResultType.ARRAY) {
            return TestStatus(
                passed = false,
                value = pathResult.value,
                expected = expected,
                exception = Exception("The Value should be an array.")
            )
        }

        val value = (pathResult.value as Iterable<*>).map { it.toString() }.sorted()
        val entered = (expected as List<*>).map { it.toString() }.sorted()

        val isEqual = value.size == entered.size && value.toSet() == entered.toSet()

        val error: Exception? =
            if (isEqual) null else Exception(
                "Expected: [${expected.joinToString(",")}] is not equal to [${
                    value.joinToString(
                        ","
                    )
                }]"
            )

        return TestStatus(
            passed = isEqual, value = value, expected = expected, exception = error
        )
    }

    companion object {
        const val Name = TestTypes.Equals
    }
}