package com.sevenreup.fhir.core.tests.operations

import com.sevenreup.fhir.core.tests.inputs.TestTypes

class StartsWithNoCase : StringOperation() {
    override fun getName(): String {
        return TestTypes.StartsWithNoCase
    }

    override fun calculateValue(value: String, expected: String): Boolean {
        return value.startsWith(expected, ignoreCase = true)
    }
}