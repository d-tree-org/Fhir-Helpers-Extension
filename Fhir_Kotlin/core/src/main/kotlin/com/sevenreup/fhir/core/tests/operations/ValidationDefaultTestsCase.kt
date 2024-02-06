package com.sevenreup.fhir.core.tests.operations

import ca.uhn.fhir.validation.FhirValidator
import ca.uhn.fhir.validation.ResultSeverityEnum
import ca.uhn.fhir.validation.ValidationResult
import com.sevenreup.fhir.core.models.DefaultTestResult
import com.sevenreup.fhir.core.models.TestStatus
import com.sevenreup.fhir.core.tests.inputs.DefaultTestTypes
import org.hl7.fhir.r4.model.Bundle

class ValidationDefaultTestsCase(private val validator: FhirValidator) {
    fun validate(bundle: Bundle, path: String): DefaultTestResult {
        var passed = true
        val testResults = mutableListOf<TestStatus>()
        for (entry in bundle.entry) {
            val result = validator.validateWithResult(entry.resource)
            if (!result.isSuccessful) passed = false
            testResults.add(
                TestStatus(
                    passed = result.isSuccessful,
                    value = DefaultTestTypes.Validation,
                    expected = "",
                    exception = Exception(result.errorMessages).apply {
                         this.stackTrace = arrayOf()
                    },
                    path = path
                )
            )
        }

        return DefaultTestResult(passed, testResults)
    }
}

val ValidationResult.errorMessages
    get() = buildString {
        for (validationMsg in
        messages.filter { it.severity.ordinal >= ResultSeverityEnum.WARNING.ordinal }) {
            appendLine("${validationMsg.message} - ${validationMsg.locationString}")
        }
    }