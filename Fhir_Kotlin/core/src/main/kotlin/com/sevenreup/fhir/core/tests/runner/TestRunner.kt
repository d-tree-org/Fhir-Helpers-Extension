package com.sevenreup.fhir.core.tests.runner

import ca.uhn.fhir.parser.IParser
import ca.uhn.fhir.validation.FhirValidator
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.sevenreup.fhir.core.compiler.parsing.ParseJsonCommands
import com.sevenreup.fhir.core.config.CompileMode
import com.sevenreup.fhir.core.config.ProjectConfig
import com.sevenreup.fhir.core.config.ProjectConfigManager
import com.sevenreup.fhir.core.models.*
import com.sevenreup.fhir.core.tests.inputs.*
import com.sevenreup.fhir.core.tests.operations.*
import com.sevenreup.fhir.core.utils.getParentPath
import net.minidev.json.JSONArray
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.utils.StructureMapUtilities

class TestRunner(
    private val configManager: ProjectConfigManager, private val parser: ParseJsonCommands,
    private val iParser: IParser,
    private val scu: StructureMapUtilities,
    private val contextR4: SimpleWorkerContext,
    private val validator: FhirValidator,
) {
    private lateinit var configs: ProjectConfig
    fun startTestList(path: String, projectRoot: String?): MapTestResult {
        val testData = TestFileHelpers.readTestFile(path)

        val responseTestResults = mutableListOf<ResponseTestResult>()

        var passedFiles = 0
        var failedFiles = 0

        var passedTestCount = 0
        var failedTestCount = 0

        for (test in testData.tests) {
            val status = mutableListOf<TestStatus>()

            var passedTests = 0
            var failedTests = 0

            val pair = runTests(
                tests = test.verify,
                path = path,
                response = test.response,
                defaultTests = testData.map.defaultTests,
                projectRoot = projectRoot
            )
            pair.first.map { testResult ->
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

            passedTestCount += passedFiles
            failedTestCount += failedFiles

            responseTestResults.add(
                ResponseTestResult(
                    file = test.response,
                    tests = test.verify.size,
                    passed = passedTests,
                    failed = failedTests,
                    testResults = status,
                    defaultTestsResults = pair.second
                )
            )
        }

        return MapTestResult(
            responseTestResults,
            failed = failedFiles,
            passed = passedFiles,
            files = testData.tests.size,
            allFailedTests = failedTestCount,
            allPassedTests = passedTestCount
        )
    }

    fun targetTest(
        path: String, data: TestCaseData, defaultTests: List<DefaultTests>, projectRoot: String? = null
    ): TestStatusData {
        val results = runTests(
            tests = listOf(
                TestVerify.fromTestCaseData(data)
            ), path = path, response = data.response, defaultTests = defaultTests, projectRoot = projectRoot
        )
        return TestStatusData(
            results.first.first(),
            results.second
        )
    }

    private fun runTests(
        tests: List<TestVerify>,
        path: String,
        response: String,
        defaultTests: List<DefaultTests>,
        projectRoot: String? = null
    ): Pair<List<TestStatus>, List<DefaultTestResult>> {
        val testData = TestFileHelpers.readTestFile(path)
        configs = configManager.loadProjectConfig(projectRoot, path.getParentPath())
        val bundle = parser.parseBundle(iParser, contextR4, scu, path.getParentPath(), testData.map, response, configs)
        val jsonString = iParser.encodeResourceToString(bundle.data)

        if (configs.compileMode != CompileMode.Production) {
            println("----- Response ------")
            println(jsonString)
            println("----- End Response ------")
        }

        val document = Configuration.defaultConfiguration().jsonProvider().parse(jsonString)

        val results = mutableListOf<TestStatus>()
        val defaultTestsResults = mutableListOf<DefaultTestResult>()

        for (verify in tests) {
            results.add(
                try {
                    runTestCase(
                        document,
                        verify,
                    )
                } catch (e: Exception) {
                    TestStatus(false, null, null, e, verify.path)
                }
            )
            defaultTestsResults.addAll(runDefaultTestSuite(bundle.data, defaultTests, verify))
        }

        return Pair(results, defaultTestsResults)
    }

    private fun runTestCase(document: Any, verify: TestVerify): TestStatus {
        var testResult: TestStatus
        var result: PathResult? = null

        try {
            var valueType: ValueTypes = ValueTypes.String

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
                TestTypes.ArrayEquals -> ArrayEquals().apply {
                    valueType = ValueTypes.Array
                }

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
                    valueType = ValueTypes.Range
                    Between()
                }

                TestTypes.StartsWith -> StartsWith()
                TestTypes.StartsWithNoCase -> StartsWithNoCase()
                TestTypes.EndsWith -> EndsWith()
                TestTypes.EndsWithNoCase -> EndsWithNoCase()
                else -> null
            }

            testResult = if (operation != null) {
                operation.execute(value = result, expected = verify.actualValue(valueType))
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
            testResult = TestStatus(false, value = result, expected = verify.value, exception = e, path = verify.path)
        }

        return testResult
    }

    private fun runDefaultTestSuite(
        bundle: Bundle,
        defaultTests: List<DefaultTests>,
        verify: TestVerify
    ): List<DefaultTestResult> {
        val results = mutableListOf<DefaultTestResult>()
        for (test in defaultTests) {
            val result = when (test.type) {
                DefaultTestTypes.Validation -> {
                    ValidationDefaultTestsCase(validator).validate(bundle, verify.path)
                }

                else -> null
            }

            result?.let { results.add(it) }
        }

        return results
    }
}