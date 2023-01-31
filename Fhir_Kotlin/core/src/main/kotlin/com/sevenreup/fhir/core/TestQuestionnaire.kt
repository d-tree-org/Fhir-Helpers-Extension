package com.sevenreup.fhir.core

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.sevenreup.fhir.core.utilities.TransformSupportServices
import com.sevenreup.fhir.core.utils.readFile
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Parameters
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.utils.StructureMapUtilities
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager
import org.hl7.fhir.utilities.npm.ToolsVersion

fun parseBundle(structureMap: String, questionnaireResponsePath: String) {
    val questionnaireData = questionnaireResponsePath.readFile()

    val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    val pcm = FilesystemPackageCacheManager(true, ToolsVersion.TOOLS_VERSION)
    val contextR4 = SimpleWorkerContext.fromPackage(pcm.loadPackage("hl7.fhir.r4.core", "4.0.1"))
    contextR4.setExpansionProfile(Parameters())
    contextR4.isCanRunWithoutTerminology = true
    val targetResource = Bundle()
    val scu = StructureMapUtilities(contextR4, TransformSupportServices(contextR4))
    val baseElement =
        iParser.parseResource(QuestionnaireResponse::class.java, questionnaireData)
    val map = scu.parse(structureMap.readFile(), "Main")

    scu.transform(contextR4, baseElement, map, targetResource)
    println(iParser.encodeResourceToString(targetResource))
}