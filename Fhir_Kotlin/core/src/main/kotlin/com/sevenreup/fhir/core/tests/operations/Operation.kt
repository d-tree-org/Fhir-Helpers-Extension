package com.sevenreup.fhir.core.tests.operations

import com.sevenreup.fhir.core.models.TestStatus
import com.sevenreup.fhir.core.tests.inputs.PathResult

interface Operation {
    fun execute(value: PathResult?, expected: Any?): TestStatus
}