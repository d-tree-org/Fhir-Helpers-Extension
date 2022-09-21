package com.sevenreup.fhir.compiler

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import org.hl7.fhir.r4.model.Questionnaire
import com.sevenreup.fhir.compiler.structure_maps.createStructureMapFromFile
import readFile

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        when (args[0]) {
            "str_compile" -> {
                val path = args[1]
                val srcName = args[2]

                compileStructureMap(path, srcName)
            }

            "qst_verify" -> {
                val path = args[1]
                verifyQuestionnaire(path)
            }

            "fmt_str" -> {
                val path = args[1]
                val srcName = args[2]

                formatStructureMap(path, srcName)
            }
            "tests" -> {
                ImportTests()
            }
            else -> {
                throw Exception("Please use a valid mode")
            }
        }

    } else {
        throw Exception("Please specify args")
    }
}

fun compileStructureMap(path: String, srcName: String) {
    val map = createStructureMapFromFile(path, srcName)
    val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    val mapString = iParser.encodeResourceToString(map)

    println("\n\nMAP_OUTPUT_STARTS_HERE\n\n")
    println(mapString)
}


fun formatStructureMap(path: String, srcName: String) {
    val map = createStructureMapFromFile(path, srcName)

    println("\n\nMAP_OUTPUT_STARTS_HERE\n\n")
    println(org.hl7.fhir.r4.utils.StructureMapUtilities.render(map))
}



fun verifyQuestionnaire(path: String) {
    val content = path.readFile()
    val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    val validator = FhirContext.forCached(FhirVersionEnum.R4).newValidator()
    val questionnaire = iParser.parseResource(Questionnaire::class.java, content)
    validator.validateWithResult(questionnaire)
    println(questionnaire.toString())
}