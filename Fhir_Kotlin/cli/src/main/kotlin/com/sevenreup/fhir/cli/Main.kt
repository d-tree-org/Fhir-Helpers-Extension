package com.sevenreup.fhir.cli

import com.sevenreup.fhir.cli.commands.runTests
import com.sevenreup.fhir.core.compiler.ResourceParser
import com.sevenreup.fhir.core.config.ProjectConfigManager
import com.sevenreup.fhir.core.parseBundle
import com.sevenreup.fhir.core.utils.formatStructureMap
import com.sevenreup.fhir.core.utils.strJsonToMap
import com.sevenreup.fhir.core.utils.verifyQuestionnaire

fun main(args: Array<String>) {
    val parser = ResourceParser(configManager = ProjectConfigManager())
    if (args.isNotEmpty()) {
        when (args[0]) {
            "compile" -> {
                val path = args[1]
                val projectRoot = args.getOrNull(2)

                val resString = parser.parseStructureMapFromMap(path, projectRoot)

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

            "transform" -> {
                val path = args[1]
                val srcName = args[2]

                parseBundle(path, srcName)
            }

            "transform_batch" -> {
                val path = args[1]
                val projectRoot = args.getOrNull(2)

                val data = parser.parseTransformFromJson(path,projectRoot)
                data.data?.entries?.forEach {
                    println("-----" + it.key + "----")
                    println(it.value)
                }
            }

            "test" -> {
                val path = args[1]
                val projectRoot = args.getOrNull(2)

                runTests(path, projectRoot)
            }

            else -> {
                throw Exception("Please use a valid mode")
            }
        }

    } else {
        throw Exception("Please specify args")
    }
}