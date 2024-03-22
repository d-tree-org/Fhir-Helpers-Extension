package com.sevenreup.fhir.core.fhir

import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Parameters
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager


object FhirConfigs {
    private const val HL7_FHIR_PACKAGE = "hl7.fhir.r4.core"
    private const val HL7_FHIR_PACKAGE_VERSION = "4.0.1"
    fun createWorkerContext(): SimpleWorkerContext {
        val pcm = FilesystemPackageCacheManager(true)
        return  SimpleWorkerContext.fromPackage(pcm.loadPackage(HL7_FHIR_PACKAGE, HL7_FHIR_PACKAGE_VERSION)).apply {
            setExpansionProfile(Parameters())
            isCanRunWithoutTerminology = true
        }
    }
}