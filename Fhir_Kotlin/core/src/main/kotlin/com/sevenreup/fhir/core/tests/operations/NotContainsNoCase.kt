package com.sevenreup.fhir.core.tests.operations

import com.sevenreup.fhir.core.tests.inputs.TestTypes

class NotContainsNoCase : StringOperation() {
    override fun getName(): String {
        return TestTypes.NotContainsNoCase
    }

    override fun calculateValue(value: String, expected: String): Boolean {
        return !value.contains(expected, ignoreCase = true)
    }
}