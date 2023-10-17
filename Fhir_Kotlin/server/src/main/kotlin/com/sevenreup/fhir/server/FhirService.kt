package com.sevenreup.fhir.server

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcMethod
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcOptional
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
    fun callCompileStructureMap(
        @JsonRpcParam("path") path: String,
        @JsonRpcOptional @JsonRpcParam("projectRoot") root: String? = null
    ): String {
        try {
            val res = parser.parseStructureMapFromMap(path, root)

            if (res.error != null) {
                throw Exception(res.error)
            }

            return res.data!!
        } catch (e: Exception) {
            throw e
        }
    }

    @JsonRpcMethod("formatStructureMap")
    fun callFormatStructureMap(
        @JsonRpcParam("path") path: String,
        @JsonRpcOptional @JsonRpcParam("projectRoot") root: String? = null
    ): String {
        try {
            val res = formatStructureMap(path, root)

            if (res.error != null) {
                throw Exception(res.error)
            }

            return res.data!!
        } catch (e: Exception) {
            throw e
        }
    }

    @JsonRpcMethod("parseTransformFromJson")
    fun callTransformBatch(
        @JsonRpcParam("path") path: String,
        @JsonRpcOptional @JsonRpcParam("projectRoot") root: String? = null
    ): Map<String, String> {
        try {
            val res = parser.parseTransformFromJson(path, root)

            if (res.error != null) {
                throw Exception(res.error)
            }

            return res.data!!
        } catch (e: Exception) {
            throw e
        }
    }

    @JsonRpcMethod("runTest")
    fun callRunTest(
        @JsonRpcParam("path") path: String,
        @JsonRpcOptional @JsonRpcParam("projectRoot") root: String? = null,
        @JsonRpcParam("data") data: TestCaseData
    ): TestStatus {
        try {
            val runner = StructureMapTests(configManager, ParseJsonCommands())
            return runner.targetTest(path, data, root)
        } catch (e: Exception) {
            throw e
        }
    }
}
