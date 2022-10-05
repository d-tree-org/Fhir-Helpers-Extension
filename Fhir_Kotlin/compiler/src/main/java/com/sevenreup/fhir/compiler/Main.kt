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

            "quest_test" -> {
                val path = args[1]
                val srcName = args[2]

                TestQuestionnaire(path, srcName)
            }

            else -> {
                throw Exception("Please use a valid mode")
            }
        }

    } else {
        throw Exception("Please specify args")
    }
}