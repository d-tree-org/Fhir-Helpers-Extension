package com.sevenreup.fhir.core.tests.operations

import com.sevenreup.fhir.core.tests.inputs.TestTypes
import java.math.BigDecimal

class LessThan : NumberOperations() {

    override fun getName(): String {
        return TestTypes.LessThan
    }

    override fun calculateValue(value: BigDecimal, expected: BigDecimal): Boolean {
        return value < expected
    }
}