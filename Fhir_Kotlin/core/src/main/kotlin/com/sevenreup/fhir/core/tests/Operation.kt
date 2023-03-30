package com.sevenreup.fhir.core.tests

interface Operation {
    fun execute(value: Any, expected: Any): TestStatus
}