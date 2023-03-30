package com.sevenreup.fhir.core.tests

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.google.gson.Gson
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.sevenreup.fhir.core.models.MapConfig
import com.sevenreup.fhir.core.parsing.ParseJsonCommands
import com.sevenreup.fhir.core.tests.operations.*
import com.sevenreup.fhir.core.utilities.TransformSupportServices
import com.sevenreup.fhir.core.utils.getParentPath
import com.sevenreup.fhir.core.utils.readFile
import net.minidev.json.JSONArray
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Parameters
import org.hl7.fhir.r4.utils.StructureMapUtilities
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager
import org.hl7.fhir.utilities.npm.ToolsVersion

object StructureMapTests {
    fun test(path: String): TestResult {
        val rawJson = path.readFile()
        val gson = Gson()
        val config = gson.fromJson(rawJson, JsonConfig::class.java)

        val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
        val pcm = FilesystemPackageCacheManager(true, ToolsVersion.TOOLS_VERSION)
        val contextR4 = SimpleWorkerContext.fromPackage(pcm.loadPackage("hl7.fhir.r4.core", "4.0.1"))
        contextR4.setExpansionProfile(Parameters())
        contextR4.isCanRunWithoutTerminology = true
        val scu = StructureMapUtilities(contextR4, TransformSupportServices(contextR4))
        val fileTestResults = mutableListOf<FileTestResult>()

        var passedFiles = 0
        var failedFiles = 0

        for (test in config.tests) {
            val bundle =
                ParseJsonCommands.parseBundle(iParser, contextR4, scu, path.getParentPath(), config.map, test.response)
            val jsonString = iParser.encodeResourceToString(bundle.data)
            println(jsonString)

            val document =
                Configuration.defaultConfiguration().jsonProvider().parse(jsonString)
            val status = mutableListOf<TestStatus>()

            var passedTests = 0
            var failedTests = 0

            for (verify in test.verify) {
                var result: String? = null
                var testResult: TestStatus

                try {
                    val resultRaw: Any = JsonPath.read(document, verify.path)
                    result = if (resultRaw is JSONArray) {
                        resultRaw.firstOrNull()?.toString() ?: ""
                    } else {
                        resultRaw.toString()
                    }

                    val operation = when (verify.type) {
                        TestTypes.Equals -> EqualsTo()
                        TestTypes.EqualsNoCase -> EqualsToNoCase()
                        TestTypes.NotEquals -> NotEqualsTo()
                        TestTypes.LessThan -> LessThan()
                        TestTypes.LessThanOrEqual -> LessThanOrEqual()
                        TestTypes.GreaterThan -> GreaterThan()
                        TestTypes.GreaterThanOrEqual -> GreaterThanOrEqual()
                        TestTypes.Contains -> Contains()
                        TestTypes.NotContains -> NotContains()
                        TestTypes.ContainsNoCase -> ContainsNoCase()
                        TestTypes.NotContainsNoCase -> NotContainsNoCase()
                        TestTypes.Null -> Null()
                        TestTypes.NotNull -> NotNull()
                        // TODO: Null
                        // TODO: NotNull
                        // TODO: Between
                        TestTypes.StartsWith -> StartsWith()
                        TestTypes.StartsWithNoCase -> StartsWithNoCase()
                        TestTypes.EndsWith -> EndsWith()
                        TestTypes.EndsWithNoCase -> EndsWithNoCase()
                        else -> null
                    }

                    testResult = if (operation != null) {
                        operation.execute(value = result, expected = verify.value)
                    } else {
                        val err = Exception("Assertion not supported")
                        TestStatus(
                            passed = false, value = result, expected = verify.value, exception = err
                        )
                    }

                } catch (e: Exception) {
                    if (e is ClassCastException) {
                        val failedToCastToString = e.message?.contains("Array")
                        println(failedToCastToString)
                        // TODO: Work on array
                    }
                    testResult = TestStatus(false, value = result, expected = verify.value, exception = e)
                }
                if (testResult.passed)
                    passedTests++ else failedTests++
                status.add(
                    testResult
                )
            }

            if (failedTests > 0) {
                failedFiles++
            } else {
                passedFiles++
            }
            fileTestResults.add(
                FileTestResult(
                    file = test.response,
                    tests = test.verify.size,
                    passed = passedTests,
                    failed = failedTests,
                    testResults = status
                )
            )
        }

        return TestResult(
            fileTestResults,
            failed = failedFiles,
            passed = passedFiles,
            files = config.tests.size
        )
    }
}

data class FileTestResult(
    val file: String,
    val tests: Int,
    val passed: Int,
    val failed: Int,
    val testResults: List<TestStatus>
)

data class TestStatus(
    val passed: Boolean,
    val value: Any? = null,
    val expected: Any? = null,
    val exception: Exception? = null
) {
    fun createException(message: String) {

    }
}

data class TestResult(
    val fileResults: List<FileTestResult>,
    val failed: Int,
    val passed: Int,
    val files: Int
)

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