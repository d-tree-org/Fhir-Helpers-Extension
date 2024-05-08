package com.sevenreup.fhir.core.utils

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.sevenreup.fhir.core.structureMaps.createStructureMapFromFile
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager
import org.hl7.fhir.utilities.npm.ToolsVersion

fun formatStructureMap(path: String, srcName: String?): CoreResponse<String> {
    val map = createStructureMapFromFile(path, srcName ?: "Main")

    return CoreResponse(data = org.hl7.fhir.r4.utils.StructureMapUtilities.render(map))
}

fun verifyQuestionnaire(path: String) {
    val content = path.readFile()
    val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    val validator = FhirContext.forCached(FhirVersionEnum.R4).newValidator()
    val questionnaire = iParser.parseResource(Questionnaire::class.java, content)
    validator.validateWithResult(questionnaire)
    println(questionnaire.toString())
}

fun Reference.extractId(): String =
    if (this.reference.isNullOrEmpty()) {
        ""
    } else this.reference.substringAfterLast(delimiter = '/', missingDelimiterValue = "")
