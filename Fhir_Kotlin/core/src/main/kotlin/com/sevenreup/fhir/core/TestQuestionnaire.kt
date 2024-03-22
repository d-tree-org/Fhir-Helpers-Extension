package com.sevenreup.fhir.core

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.sevenreup.fhir.core.fhir.FhirConfigs
import com.sevenreup.fhir.core.utilities.TransformSupportServices
import com.sevenreup.fhir.core.utils.readFile
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.utils.StructureMapUtilities

fun parseBundle(structureMap: String, questionnaireResponsePath: String) {
    val questionnaireData = questionnaireResponsePath.readFile()

    val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    val contextR4 = FhirConfigs.createWorkerContext()
    val targetResource = Bundle()
    val scu = StructureMapUtilities(contextR4, TransformSupportServices(contextR4))
    val baseElement =
        iParser.parseResource(QuestionnaireResponse::class.java, questionnaireData)
    val map = scu.parse(structureMap.readFile(), "Main")

    scu.transform(contextR4, baseElement, map, targetResource)
    println(iParser.encodeResourceToString(targetResource))
}