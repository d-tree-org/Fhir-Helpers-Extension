package com.sevenreup.fhir.core.tests.operations

import com.sevenreup.fhir.core.tests.inputs.TestTypes

class StartsWith : StringOperation() {
    override fun getName(): String {
        return TestTypes.StartsWith
    }

    override fun calculateValue(value: String, expected: String): Boolean {
        return value.startsWith(expected)
    }
}