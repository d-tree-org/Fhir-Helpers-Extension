package com.sevenreup.fhir.cli

import com.sevenreup.fhir.cli.commands.runTests
import com.sevenreup.fhir.core.compiler.ResourceParser
import com.sevenreup.fhir.core.config.ProjectConfigManager
import com.sevenreup.fhir.core.parseBundle
import com.sevenreup.fhir.core.uploader.ConfigUploader
import com.sevenreup.fhir.core.uploader.FileUploader
import com.sevenreup.fhir.core.uploader.LocationHierarchyUploader
import com.sevenreup.fhir.core.utils.formatStructureMap
import com.sevenreup.fhir.core.utils.verifyQuestionnaire
import kotlinx.coroutines.runBlocking
import picocli.CommandLine
import picocli.CommandLine.*
import java.util.concurrent.Callable
import kotlin.system.exitProcess

fun createParser() = ResourceParser(configManager = ProjectConfigManager())

@Command(name = "compile")
class CompileCommand : Callable<Int> {
    @Parameters(index = "0", description = ["Path to the StructureMap file"])
    lateinit var path: String

    @Option(names = ["-r", "--root"], description = [Constants.rootDescription])
    var projectRoot: String? = null
    override fun call(): Int {
        val parser = createParser()
        val resString = parser.parseStructureMapFromMap(path, projectRoot)

        println("\n\nMAP_OUTPUT_STARTS_HERE\n\n")
        println(resString.data)
        return 0
    }
}

@Command(name = "test")
class TestCommand : Callable<Int> {
    @Parameters(index = "0", description = ["Path to the test file"])
    lateinit var path: String

    @Option(names = ["-r", "--root"], description = [Constants.rootDescription])
    var projectRoot: String? = null

    @Option(names = ["-w", "--watch"], description = [Constants.watchDescription])
    var watch: Boolean = false

    override fun call(): Int {
        runTests(path, watch, projectRoot)
        return 0
    }
}

@Command(name = "upload")
class UploaderCommand : Callable<Int> {
    @Parameters(index = "0", description = ["Path to the test file"])
    lateinit var path: String

    @Option(names = ["-r", "--root"], description = [Constants.rootDescription])
    lateinit var projectRoot: String

    @Option(names = ["-s", "--server"], description = [Constants.server])
    lateinit var fhirServerUrl: String

    @Option(names = ["-k", "--apiKey"], description = [Constants.rootDescription])
    lateinit var fhirServerUrlApiKey: String

    override fun call(): Int {
        runBlocking {
            FileUploader(fhirServerUrl, fhirServerUrlApiKey).batchUpload(path, projectRoot)
        }
        return 0
    }
}

@Command(name = "configUploader")
class AppConfigUploaderCommand : Callable<Int> {
    @Parameters(index = "0", description = ["The environment"])
    lateinit var environment: String

    @Option(names = ["-r", "--root"], description = [Constants.rootDescription])
    lateinit var projectRoot: String

    @Option(names = ["-s", "--server"], description = [Constants.server])
    lateinit var fhirServerUrl: String

    @Option(names = ["-k", "--apiKey"], description = [Constants.rootDescription])
    lateinit var fhirServerUrlApiKey: String

    override fun call(): Int {
        runBlocking {
            ConfigUploader(fhirServerUrl, fhirServerUrlApiKey).upload(environment, projectRoot, projectRoot)
        }
        return 0
    }
}

@Command(name = "locationUploader")
class LocationHierarchyUploaderCommand : Callable<Int> {
    @Parameters(index = "0", description = ["The environment"])
    lateinit var environment: String

    @Option(names = ["-r", "--root"], description = [Constants.rootDescription])
    lateinit var projectRoot: String

    @Option(names = ["-p", "--path"], description = [Constants.rootDescription])
    lateinit var path: String

    @Option(names = ["-s", "--server"], description = [Constants.server])
    lateinit var fhirServerUrl: String

    @Option(names = ["-k", "--apiKey"], description = [Constants.rootDescription])
    lateinit var fhirServerUrlApiKey: String

    override fun call(): Int {
        runBlocking {
            LocationHierarchyUploader(fhirServerUrl, fhirServerUrlApiKey).upload(environment, path, projectRoot)
        }
        return 0
    }
}

@Command(name = "qst_verify")
class QuestVerifyCommand : Callable<Int> {
    @Parameters(index = "0", description = ["Path to the questionnaire"])
    lateinit var path: String

    override fun call(): Int {
        verifyQuestionnaire(path)
        return 0
    }
}

@Command(name = "fmt_str")
class FmtStrCommand : Callable<Int> {
    @Parameters(index = "0", description = ["Path to the StructureMap File"])
    lateinit var path: String

    @Option(names = ["-src", "--source"])
    var srcName: String? = null
    override fun call(): Int {
        val data = formatStructureMap(path, srcName)
        println("\n\nMAP_OUTPUT_STARTS_HERE\n\n")
        println(data.data)
        return 0
    }
}

@Command(name = "transform")
class TransformCommand : Callable<Int> {
    @Parameters(index = "0", description = ["Path to the StructureMap File"])
    lateinit var path: String

    @Parameters(index = "1")
    lateinit var questionnaire: String
    override fun call(): Int {
        parseBundle(path, questionnaire)
        return 0
    }
}

@Command(name = "transform_batch")
class TransFormBatchCommand : Callable<Int> {
    @Parameters(index = "0", description = ["Path to the config"])
    lateinit var path: String

    @Option(names = ["-r", "--root"], description = [Constants.rootDescription])
    var projectRoot: String? = null
    override fun call(): Int {
        val parser = createParser()
        val data = parser.parseTransformFromJson(path, projectRoot)
        data.data?.entries?.forEach {
            println("-----" + it.key + "----")
            println(it.value)
        }
        return 0
    }
}

@Command(
    subcommands = [
        TestCommand::class, CompileCommand::class, TransformCommand::class,
        TransFormBatchCommand::class, QuestVerifyCommand::class, FmtStrCommand::class,
        UploaderCommand::class, AppConfigUploaderCommand::class, LocationHierarchyUploaderCommand::class]
)
class RunCommand : Callable<Int> {
    override fun call(): Int {
        return 0
    }
}

fun main(args: Array<String>) {
    exitProcess(CommandLine(RunCommand()).execute(*args))
}