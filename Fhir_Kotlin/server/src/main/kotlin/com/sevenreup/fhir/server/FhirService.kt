package com.sevenreup.fhir.server

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcMethod
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcParam
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcService
import com.sevenreup.fhir.core.compiler.ResourceParser
import com.sevenreup.fhir.core.compiler.parsing.ParseJsonCommands
import com.sevenreup.fhir.core.config.ProjectConfigManager
import com.sevenreup.fhir.core.tests.StructureMapTests
import com.sevenreup.fhir.core.tests.TestStatus
import com.sevenreup.fhir.core.tests.inputs.TestCaseData
import com.sevenreup.fhir.core.utils.formatStructureMap

@JsonRpcService
class FhirService {
    private var parser = ResourceParser(ProjectConfigManager())
    private val configManager = ProjectConfigManager()

    @JsonRpcMethod("compileStructureMap")
    fun callCompileStructureMap(@JsonRpcParam("path") path: String): String {
        try {
            val res = parser.parseStructureMapFromMap(path, null)

            if (res.error != null) {
                throw Exception(res.error)
            }

            return res.data!!
        } catch (e: Exception) {
            throw Exception("Something went wrong")
        }
    }

    @JsonRpcMethod("formatStructureMap")
    fun callFormatStructureMap(@JsonRpcParam("path") path: String): String {
        try {
            val res = formatStructureMap(path, null)

            if (res.error != null) {
                throw Exception(res.error)
            }

            return res.data!!
        } catch (e: Exception) {
            throw Exception("Something went wrong")
        }
    }

    @JsonRpcMethod("parseTransformFromJson")
    fun callTransformBatch(@JsonRpcParam("path") path: String): Map<String, String> {
        try {
            val res = parser.parseTransformFromJson(path, null)

            if (res.error != null) {
                throw Exception(res.error)
            }

            return res.data!!
        } catch (e: Exception) {
            throw Exception("Something went wrong")
        }
    }

    @JsonRpcMethod("runTest")
    fun callRunTest(@JsonRpcParam("path") path: String, @JsonRpcParam("data") data: TestCaseData): TestStatus {
        try {
            val runner = StructureMapTests(configManager, ParseJsonCommands())
            println(path)
            return runner.targetTest(path, data)
        } catch (e: Exception) {
            throw Exception("Something went wrong")
        }
    }
}
