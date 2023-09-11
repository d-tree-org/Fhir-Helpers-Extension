package com.sevenreup.fhir.core.tests

import com.sevenreup.fhir.core.tests.inputs.PathResult

interface Operation {
    fun execute(value: PathResult?, expected: Any?): TestStatus
}