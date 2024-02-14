package com.sevenreup.fhir.core.tests

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport
import ca.uhn.fhir.context.support.IValidationSupport
import ca.uhn.fhir.parser.IParser
import ca.uhn.fhir.validation.FhirValidator
import com.sevenreup.fhir.core.compiler.parsing.ParseJsonCommands
import com.sevenreup.fhir.core.config.ProjectConfigManager
import com.sevenreup.fhir.core.models.*
import com.sevenreup.fhir.core.tests.inputs.TestCaseData
import com.sevenreup.fhir.core.tests.runner.TestFileHelpers
import com.sevenreup.fhir.core.tests.runner.TestRunner
import com.sevenreup.fhir.core.utilities.TransformSupportServices
import com.sevenreup.fhir.core.utils.asWatchChannel
import com.sevenreup.fhir.core.utils.toAbsolutePath
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.flow
import org.hl7.fhir.common.hapi.validation.support.*
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Parameters
import org.hl7.fhir.r4.utils.StructureMapUtilities
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager
import org.hl7.fhir.utilities.npm.ToolsVersion
import java.io.File


class StructureMapTests(private val configManager: ProjectConfigManager, private val parser: ParseJsonCommands) {

    private val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    private val scu: StructureMapUtilities
    private var contextR4: SimpleWorkerContext
    private var validator: FhirValidator

    init {
        val pcm = FilesystemPackageCacheManager(true, ToolsVersion.TOOLS_VERSION)
        contextR4 = SimpleWorkerContext.fromPackage(pcm.loadPackage("hl7.fhir.r4.core", "4.0.1"))
        contextR4.setExpansionProfile(Parameters())
        contextR4.isCanRunWithoutTerminology = true
        scu = StructureMapUtilities(contextR4, TransformSupportServices(contextR4))
        validator = provideFhirValidator()
    }

    fun targetTest(path: String, data: TestCaseData, projectRoot: String? = null): TestStatusData {
        val testRunner = TestRunner(configManager, parser, iParser, scu, contextR4, validator)
        return testRunner.targetTest(path, data, data.defaultTests, projectRoot)
    }

    suspend fun watchTestChanges(path: String, projectRoot: String?) = flow<TestResult> {
        val file = File(path.toAbsolutePath())
        val channel = file.asWatchChannel()

        channel.consumeEach { event ->
            try {
                emit(runTests(path, projectRoot))
            } catch (e: Exception) {
                error(e)
            }
        }
    }

    fun runTests(path: String, projectRoot: String?): TestResult {
        val file = File(path)

        return if (file.isDirectory) {
            val files = TestFileHelpers.getAllTestFiles(path)
            startTestRun(files.map { it }, projectRoot)
        } else {
            startTestRun(listOf(file.absolutePath), projectRoot)
        }
    }

    private fun startTestRun(paths: List<String>, projectRoot: String?): TestResult {
        val results = mutableListOf<MapTestResult>()
        var passed = 0
        var failed = 0
        for (file in paths) {
            try {
                val result = test(file, projectRoot)

                if (result.failed <= 0) {
                    passed++
                } else {
                    failed++
                }

                results.add(result)
            } catch (e: Exception) {
                failed++
                results.add(
                    MapTestResult(
                        fileResults = listOf(
                            ResponseTestResult(
                                file = file,
                                tests = 1,
                                passed = 0,
                                failed = 1,
                                testResults = listOf(TestStatus(false, null, null, e, file))
                            )
                        ), failed = 1, passed = 0, files = 1, allPassedTests = 0, allFailedTests = 1
                    )
                )
            }
        }
        return TestResult(list = results, failed = failed, passed = passed, files = results.size)
    }

    private fun test(path: String, projectRoot: String?): MapTestResult {
        val testRunner = TestRunner(configManager, parser, iParser, scu, contextR4, validator)
        return testRunner.startTestList(path, projectRoot)
    }

    private fun provideFhirValidator(): FhirValidator {
        val fhirContext = FhirContext.forR4()

        val validationSupportChain =
            ValidationSupportChain(
                DefaultProfileValidationSupport(fhirContext),
                InMemoryTerminologyServerValidationSupport(fhirContext),
                CommonCodeSystemsTerminologyService(fhirContext),
                UnknownCodeSystemWarningValidationSupport(fhirContext).apply {
                    setNonExistentCodeSystemSeverity(IValidationSupport.IssueSeverity.WARNING)
                },
            )
        val instanceValidator = FhirInstanceValidator(validationSupportChain)
        instanceValidator.isAssumeValidRestReferences = true
            instanceValidator.validatorResourceFetcher
            instanceValidator.setCustomExtensionDomains("d-tree.org")
        instanceValidator.invalidateCaches()
        return fhirContext.newValidator().apply { registerValidatorModule(instanceValidator) }
    }
}