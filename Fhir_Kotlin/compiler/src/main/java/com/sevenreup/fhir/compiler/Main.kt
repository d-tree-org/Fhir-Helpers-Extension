package com.sevenreup.fhir.compiler

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        when (args[0]) {
            "compile" -> {
                val path = args[1]

                compileStructureMap(path, null)
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

            "transform" -> {
                val path = args[1]
                val srcName = args[2]

                parseBundle(path, srcName)
            }

            "transform_batch" -> {
                val path = args[1]
                ParseJsonCommands.parse(path)
            }

            else -> {
                throw Exception("Please use a valid mode")
            }
        }

    } else {
        throw Exception("Please specify args")
    }
}