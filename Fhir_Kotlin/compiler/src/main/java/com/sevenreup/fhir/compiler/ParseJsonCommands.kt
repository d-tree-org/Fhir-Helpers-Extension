package com.sevenreup.fhir.compiler

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.google.gson.Gson
import com.sevenreup.fhir.compiler.utilities.TransformSupportServices
import org.apache.commons.io.FilenameUtils
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Parameters
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.utils.StructureMapUtilities
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager
import org.hl7.fhir.utilities.npm.ToolsVersion

object ParseJsonCommands {
    fun parse(path: String) {
        val rawJson = path.readFile()
        val gson = Gson()
        val config = gson.fromJson(rawJson, JsonConfig::class.java)
        val list = parseBundle(path.getParentPath(), config.map, config.response)
        list.forEach {
            println("\n--------Start {${FilenameUtils.getName(it.file)}}--------\n")
            println(it.data)
            println("\n------End----------\n")
        }
    }

    private fun parseBundle(configPath: String, map: MapConfig, resList: List<String>): List<ParseResponse> {
        val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
        val pcm = FilesystemPackageCacheManager(true, ToolsVersion.TOOLS_VERSION)
        val contextR4 = SimpleWorkerContext.fromPackage(pcm.loadPackage("hl7.fhir.r4.core", "4.0.1"))
        contextR4.setExpansionProfile(Parameters())
        contextR4.isCanRunWithoutTerminology = true
        val data = mutableListOf<ParseResponse>()
        for (path in resList) {
            println("\n----Start {${FilenameUtils.getName(path)}}-----")
            val questionnaireData = path.getAbsolutePath(configPath).readFile()

            val targetResource = Bundle()
            val scu = StructureMapUtilities(contextR4, TransformSupportServices(contextR4))
            val baseElement =
                iParser.parseResource(QuestionnaireResponse::class.java, questionnaireData)
            val strMap = scu.parse(map.path.getAbsolutePath(configPath).readFile(), map.name ?: "Main")
            scu.transform(contextR4, baseElement, strMap, targetResource)
            data.add(
                ParseResponse(
                    file = FilenameUtils.getName(path),
                    data = iParser.encodeResourceToString(targetResource)
                )
            )
            println("\n------End----------\n")
        }
        return data.toList()
    }
}

data class JsonConfig(
    val type: String,
    val map: MapConfig,
    val response: List<String>
)

data class MapConfig(
    val path: String,
    val name: String?
)

data class ParseResponse(
    val file: String,
    val data: String
)