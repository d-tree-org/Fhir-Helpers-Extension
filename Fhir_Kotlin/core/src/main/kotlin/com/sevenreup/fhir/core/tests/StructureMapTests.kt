package com.sevenreup.fhir.core.tests

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.charleskorn.kaml.Yaml
import com.google.gson.Gson
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.sevenreup.fhir.core.compiler.parsing.ParseJsonCommands
import com.sevenreup.fhir.core.config.ProjectConfigManager
import com.sevenreup.fhir.core.models.JsonConfig
import com.sevenreup.fhir.core.models.TestVerify
import com.sevenreup.fhir.core.tests.inputs.PathResult
import com.sevenreup.fhir.core.tests.inputs.PathResultType
import com.sevenreup.fhir.core.tests.inputs.TestCaseData
import com.sevenreup.fhir.core.tests.inputs.TestTypes
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


class StructureMapTests(private val configManager: ProjectConfigManager, private val parser: ParseJsonCommands) {

    private val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    private val scu: StructureMapUtilities
    private var contextR4: SimpleWorkerContext

    init {
        val pcm = FilesystemPackageCacheManager(true, ToolsVersion.TOOLS_VERSION)
        contextR4 = SimpleWorkerContext.fromPackage(pcm.loadPackage("hl7.fhir.r4.core", "4.0.1"))
        contextR4.setExpansionProfile(Parameters())
        contextR4.isCanRunWithoutTerminology = true
        scu = StructureMapUtilities(contextR4, TransformSupportServices(contextR4))
    }

    private fun loadConfigs(path: String): JsonConfig {
        return when (path.split(".").last()) {
            "json" -> {
                val rawJson = path.readFile()
                val gson = Gson()
                gson.fromJson(rawJson, JsonConfig::class.java)
            }

            "yml", "yaml" -> {
                val rawJson = path.readFile()
                Yaml.default.decodeFromString(JsonConfig.serializer(), rawJson)
            }

            else -> {
                throw Exception("File format not supported")
            }
        }
    }

    fun targetTest(path: String, data: TestCaseData, projectRoot: String? = null): TestStatus {
        val config = loadConfigs(path)
        val configs = configManager.loadProjectConfig(projectRoot, path.getParentPath())

        val bundle =
            parser.parseBundle(iParser, contextR4, scu, path.getParentPath(), config.map, data.response, configs)
        val jsonString = iParser.encodeResourceToString(bundle.data)
        println(jsonString)
        val document = Configuration.defaultConfiguration().jsonProvider().parse(jsonString)
        return runTest(
            document,
            TestVerify(type = data.type, path = data.path, value = data.value, valueRange = data.valueRange)
        )
    }

    fun test(path: String, projectRoot: String?): TestResult {
        val config = loadConfigs(path)

        val fileTestResults = mutableListOf<FileTestResult>()

        var passedFiles = 0
        var failedFiles = 0

        for (test in config.tests) {
            val configs = configManager.loadProjectConfig(projectRoot, path.getParentPath())
            val bundle =
                parser.parseBundle(iParser, contextR4, scu, path.getParentPath(), config.map, test.response, configs)
            val jsonString = iParser.encodeResourceToString(bundle.data)
            println(jsonString)

            val document = Configuration.defaultConfiguration().jsonProvider().parse(jsonString)
            val status = mutableListOf<TestStatus>()

            var passedTests = 0
            var failedTests = 0

            for (verify in test.verify) {
                val testResult = runTest(document, verify)
                if (testResult.passed) passedTests++ else failedTests++
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
            fileTestResults, failed = failedFiles, passed = passedFiles, files = config.tests.size
        )
    }


    private fun runTest(document: Any, verify: TestVerify): TestStatus {
        var testResult: TestStatus
        var result: PathResult? = null

        try {
            var useRange = false
            val resultRaw: Any = JsonPath.read(document, verify.path)
            result = if (resultRaw is JSONArray) {
                val array = resultRaw.map { it.toString() }
                if (array.isEmpty()) {
                    PathResult(PathResultType.STRING, null)
                } else if (array.size == 1) {
                    PathResult(PathResultType.STRING, array.singleOrNull())
                } else {
                    PathResult(PathResultType.ARRAY, array)
                }
            } else {
                PathResult(PathResultType.STRING, resultRaw.toString())
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
                TestTypes.Between -> {
                    useRange = true
                    Between()
                }

                TestTypes.StartsWith -> StartsWith()
                TestTypes.StartsWithNoCase -> StartsWithNoCase()
                TestTypes.EndsWith -> EndsWith()
                TestTypes.EndsWithNoCase -> EndsWithNoCase()
                else -> null
            }

            testResult = if (operation != null) {
                operation.execute(value = result, expected = if (useRange) verify.valueRange else verify.value)
                    .copy(path = verify.path)
            } else {
                val err = Exception("Assertion not supported")
                TestStatus(
                    passed = false, value = result, expected = verify.value, exception = err, path = verify.path
                )
            }

        } catch (e: Exception) {
            if (e is ClassCastException) {
                val failedToCastToString = e.message?.contains("Array")
                println(failedToCastToString)
                // TODO: Work on array
            }
            testResult =
                TestStatus(false, value = result, expected = verify.value, exception = e, path = verify.path)
        }

        return testResult
    }
}

data class FileTestResult(
    val file: String, val tests: Int, val passed: Int, val failed: Int, val testResults: List<TestStatus>
)

data class TestStatus @JvmOverloads constructor(
    val passed: Boolean,
    val value: Any? = null,
    val expected: Any? = null,
    val exception: Exception? = null,
    val path: String? = null
)

data class TestResult(
    val fileResults: List<FileTestResult>, val failed: Int, val passed: Int, val files: Int
)