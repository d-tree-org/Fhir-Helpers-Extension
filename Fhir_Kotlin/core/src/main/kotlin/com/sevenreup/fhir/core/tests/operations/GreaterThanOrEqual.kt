package com.sevenreup.fhir.core.tests.operations

import com.sevenreup.fhir.core.tests.TestTypes
import java.math.BigDecimal

class GreaterThanOrEqual: NumberOperations() {
    override fun getName(): String {
        return TestTypes.GreaterThanOrEqual
    }
    override fun calculateValue(value: BigDecimal, expected: BigDecimal): Boolean {
        return value >= expected
    }
}