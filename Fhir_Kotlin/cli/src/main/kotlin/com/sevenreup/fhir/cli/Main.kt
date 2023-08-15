package com.sevenreup.fhir.cli

import com.sevenreup.fhir.cli.commands.runTests
import com.sevenreup.fhir.core.compiler.ResourceParser
import com.sevenreup.fhir.core.config.ProjectConfigManager
import com.sevenreup.fhir.core.parseBundle
import com.sevenreup.fhir.core.utils.formatStructureMap
import com.sevenreup.fhir.core.utils.strJsonToMap
import com.sevenreup.fhir.core.utils.verifyQuestionnaire
import picocli.CommandLine
import picocli.CommandLine.*
import java.util.concurrent.Callable
import kotlin.system.exitProcess

fun createParser() = ResourceParser(configManager = ProjectConfigManager())

@Command(name = "compile")
class CompileCommand : Callable<Int> {
    @Parameters(index = "0")
    lateinit var path: String

    @Option(names = ["-r", "--root"])
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
    @Parameters(index = "0")
    lateinit var path: String

    @Option(names = ["-r", "--root"])
    var projectRoot: String? = null
    override fun call(): Int {
        runTests(path, projectRoot)
        return 0
    }
}

@Command(name = "qst_verify")
class QuestVerifyCommand : Callable<Int> {
    @Parameters(index = "0")
    lateinit var path: String

    override fun call(): Int {
        verifyQuestionnaire(path)
        return 0
    }
}

@Command(name = "fmt_str")
class FmtStrCommand : Callable<Int> {
    @Parameters(index = "0")
    lateinit var path: String

    @Option(names = ["-src", "--source"])
    var srcName: String? = null
    override fun call(): Int {

        formatStructureMap(path, srcName)
        return 0
    }
}

@Command(name = "to_map")
class ToMapCommand : Callable<Int> {
    @Parameters(index = "0")
    lateinit var path: String
    override fun call(): Int {

        strJsonToMap(path)
        return 0
    }
}

@Command(name = "transform")
class TransformCommand : Callable<Int> {
    @Parameters(index = "0")
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
    @Parameters(index = "0")
    lateinit var path: String

    @Option(names = ["-r", "--root"])
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
    subcommands = [TestCommand::class, CompileCommand::class, TransformCommand::class,
        TransFormBatchCommand::class, QuestVerifyCommand::class, FmtStrCommand::class, ToMapCommand::class]
)
class RunCommand : Callable<Int> {
    override fun call(): Int {
        return 0
    }
}

fun main(args: Array<String>) {
    exitProcess(CommandLine(RunCommand()).execute(*args))
}