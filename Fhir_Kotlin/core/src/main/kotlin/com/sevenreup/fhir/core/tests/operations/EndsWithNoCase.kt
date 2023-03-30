package com.sevenreup.fhir.core.tests.operations

import com.sevenreup.fhir.core.tests.inputs.TestTypes

class EndsWithNoCase : StringOperation() {
    override fun getName(): String {
        return TestTypes.EndsWithNoCase
    }

    override fun calculateValue(value: String, expected: String): Boolean {
        return value.endsWith(expected, ignoreCase = true)
    }
}