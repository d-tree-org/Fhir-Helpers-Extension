package com.sevenreup.fhir.cli

import com.sevenreup.fhir.core.compiler.ResourceParser
import com.sevenreup.fhir.core.importTests
import com.sevenreup.fhir.core.parseBundle
import com.sevenreup.fhir.core.parsing.ParseJsonCommands
import com.sevenreup.fhir.core.tests.StructureMapTests
import com.sevenreup.fhir.core.utils.formatStructureMap
import com.sevenreup.fhir.core.utils.strJsonToMap
import com.sevenreup.fhir.core.utils.verifyQuestionnaire

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        when (args[0]) {
            "compile" -> {
                val path = args[1]

                val resString = ResourceParser().parseStructureMapFromMap(path)

                println("\n\nMAP_OUTPUT_STARTS_HERE\n\n")
                println(resString.data)
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
            "to_map" -> {
                val path = args[1]

                strJsonToMap(path)
            }

            "import_tests" -> {
                importTests("./samples/import/sample.map")
            }

            "transform" -> {
                val path = args[1]
                val srcName = args[2]

                parseBundle(path, srcName)
            }

            "transform_batch" -> {
                val path = args[1]
                ParseJsonCommands.parse(path)
            }

            "test" -> {
                val path = args[1]
                StructureMapTests.test(path)
            }

            else -> {
                throw Exception("Please use a valid mode")
            }
        }

    } else {
        throw Exception("Please specify args")
    }
}