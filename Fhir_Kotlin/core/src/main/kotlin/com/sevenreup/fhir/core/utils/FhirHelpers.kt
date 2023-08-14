package com.sevenreup.fhir.core.utils

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.sevenreup.fhir.core.structureMaps.createStructureMapFromFile
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager
import org.hl7.fhir.utilities.npm.ToolsVersion

fun formatStructureMap(path: String, srcName: String) {
    val map = createStructureMapFromFile(path, srcName)

    println("\n\nMAP_OUTPUT_STARTS_HERE\n\n")
    println(org.hl7.fhir.r4.utils.StructureMapUtilities.render(map))
}

fun strJsonToMap(path: String) {
    val pcm = FilesystemPackageCacheManager(true, ToolsVersion.TOOLS_VERSION)
    // Package name manually checked from
    // https://simplifier.net/packages?Text=hl7.fhir.core&fhirVersion=All+FHIR+Versions
    val contextR4 = SimpleWorkerContext.fromPackage(pcm.loadPackage("hl7.fhir.r4.core", "4.0.1"))
    contextR4.isCanRunWithoutTerminology = true
    val scu = org.hl7.fhir.r4.utils.StructureMapUtilities(contextR4)
    val str = scu.parse(path.readFile(), "Main")
    org.hl7.fhir.r4.utils.StructureMapUtilities.render(str);
}


fun verifyQuestionnaire(path: String) {
    val content = path.readFile()
    val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    val validator = FhirContext.forCached(FhirVersionEnum.R4).newValidator()
    val questionnaire = iParser.parseResource(Questionnaire::class.java, content)
    validator.validateWithResult(questionnaire)
    println(questionnaire.toString())
}