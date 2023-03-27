package com.sevenreup.fhir.core.tests

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.google.gson.Gson
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.sevenreup.fhir.core.models.MapConfig
import com.sevenreup.fhir.core.parsing.ParseJsonCommands
import com.sevenreup.fhir.core.utilities.TransformSupportServices
import com.sevenreup.fhir.core.utils.getParentPath
import com.sevenreup.fhir.core.utils.readFile
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Parameters
import org.hl7.fhir.r4.utils.StructureMapUtilities
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager
import org.hl7.fhir.utilities.npm.ToolsVersion

object StructureMapTests {
    fun test(path: String) {
        val rawJson = path.readFile()
        val gson = Gson()
        val config = gson.fromJson(rawJson, JsonConfig::class.java)

        val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
        val pcm = FilesystemPackageCacheManager(true, ToolsVersion.TOOLS_VERSION)
        val contextR4 = SimpleWorkerContext.fromPackage(pcm.loadPackage("hl7.fhir.r4.core", "4.0.1"))
        contextR4.setExpansionProfile(Parameters())
        contextR4.isCanRunWithoutTerminology = true
        val scu = StructureMapUtilities(contextR4, TransformSupportServices(contextR4))

        for (test in config.tests) {
            val bundle =
                ParseJsonCommands.parseBundle(iParser, contextR4, scu, path.getParentPath(), config.map, test.response)
            val jsonString = iParser.encodeResourceToString(bundle.data)
            println(jsonString)

            val document =
                Configuration.defaultConfiguration().jsonProvider().parse(jsonString)
            for (verify in test.verify) {
                try {

                    val result: String = JsonPath.read(document, verify.path)
                    when(verify.type) {
                        "equals" -> {
                            val passed = result == verify.value
                            println("${if(passed) "✅ Passed" else "❌ Failed"}, $result")
                        }
                        else -> {

                        }
                    }
                } catch (e: Exception) {
                    print(e)
                }
            }
        }
    }
}

data class JsonConfig(
    val type: String,
    val map: MapConfig,
    val tests: List<ResTest>
)

data class ResTest(
    val response: String,
    val verify: List<TestVerify>
)

data class TestVerify(
    val type: String,
    val path: String,
    val value: String
)