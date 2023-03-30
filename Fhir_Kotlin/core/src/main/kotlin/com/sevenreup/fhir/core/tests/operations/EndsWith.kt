package com.sevenreup.fhir.core.tests.operations

import com.sevenreup.fhir.core.tests.TestTypes

class EndsWith : StringOperation() {
    override fun getName(): String {
        return TestTypes.EndsWith
    }

    override fun calculateValue(value: String, expected: String): Boolean {
        return value.endsWith(expected)
    }
}