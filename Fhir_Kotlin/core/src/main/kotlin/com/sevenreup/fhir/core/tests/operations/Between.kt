package com.sevenreup.fhir.core.tests.operations

import com.google.gson.Gson
import com.sevenreup.fhir.core.tests.Operation
import com.sevenreup.fhir.core.tests.TestStatus
import com.sevenreup.fhir.core.tests.inputs.PathResult
import com.sevenreup.fhir.core.tests.inputs.PathResultType
import com.sevenreup.fhir.core.tests.inputs.ValueRange
import com.sevenreup.fhir.core.utils.parseDate

class Between : Operation {
    override fun execute(pathResult: PathResult?, expected: Any?): TestStatus {
        if (pathResult?.type == PathResultType.ARRAY) {
            return TestStatus(
                passed = false,
                value = pathResult,
                expected = expected,
                exception = Exception("Expected Value Range but got ${expected ?: "null"}")
            )
        }
        if (expected is ValueRange? && expected != null) {
            val value = pathResult?.value
            var error: Exception? = null
            val numbers = expected.isNumber()
            if (numbers != null) {
                val valueNumber = value?.toString()?.toBigDecimalOrNull()
                    ?: return TestStatus(
                        passed = false,
                        value = value,
                        expected = expected,
                        exception = Exception("Expected field to be number but got ${value ?: "null"}")
                    )

                val passed = valueNumber >= numbers.first && valueNumber <= numbers.second

                if (!passed) {
                    error = createException(valueNumber, numbers)
                }

                return TestStatus(
                    passed = passed, value = value, expected = expected, exception = error
                )
            }
            val date = expected.isDate()
            if (date != null) {
                val valueDate = parseDate(value?.toString()) ?: return TestStatus(
                    passed = false,
                    value = value,
                    expected = expected,
                    exception = Exception("Expected field to be date but got ${value ?: "null"}")
                )
                val passed = valueDate >= date.first && valueDate <= date.second

                if (!passed) {
                    error = createException(valueDate, date)
                }

                return TestStatus(
                    passed = passed, value = value, expected = expected, exception = error
                )
            }

            return TestStatus(
                passed = false,
                value = value,
                expected = expected,
                exception = Exception(
                    "Expected Value Range but to be either dates or number but got ${
                        Gson().toJson(
                            expected
                        )
                    }"
                )
            )
        } else {
            return TestStatus(
                passed = false,
                value = pathResult,
                expected = expected,
                exception = Exception("Expected Value Range but got ${expected ?: "null"}")
            )
        }
    }

    private fun createException(value: Any, values: Pair<Any, Any>): Exception =
        Exception("Expected: $value to be between ${values.first} and ${values.second}")
}