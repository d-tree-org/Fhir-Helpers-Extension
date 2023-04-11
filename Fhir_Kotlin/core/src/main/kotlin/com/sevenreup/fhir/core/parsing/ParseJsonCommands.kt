package com.sevenreup.fhir.core.parsing

import ca.uhn.fhir.parser.IParser
import com.google.gson.Gson
import com.sevenreup.fhir.core.models.MapConfig
import com.sevenreup.fhir.core.utils.CoreResponse
import com.sevenreup.fhir.core.utils.getAbsolutePath
import com.sevenreup.fhir.core.utils.getParentPath
import com.sevenreup.fhir.core.utils.readFile
import org.apache.commons.io.FilenameUtils
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.utils.StructureMapUtilities

object ParseJsonCommands {
    fun parse(
        path: String,
        iParser: IParser,
        scu: StructureMapUtilities,
        contextR4: SimpleWorkerContext
    ): CoreResponse<Map<String, String>> {
        val rawJson = path.readFile()
        val gson = Gson()
        val config = gson.fromJson(rawJson, JsonConfig::class.java)

        val data = config.response.toHashSet().associate { res ->
            val bundle = parseBundle(iParser, contextR4, scu, path.getParentPath(), config.map, res)
            FilenameUtils.getName(bundle.file) to iParser.encodeResourceToString(bundle.data)
        }

        return CoreResponse(data = data)
    }

    fun parseBundle(
        iParser: IParser,
        contextR4: SimpleWorkerContext,
        scu: StructureMapUtilities,
        configPath: String,
        map: MapConfig,
        path: String
    ): ParseResponse {
        println("\n----Start {${FilenameUtils.getName(path)}}-----")
        val questionnaireData = path.getAbsolutePath(configPath).readFile()
        val targetResource = Bundle()
        val baseElement =
            iParser.parseResource(QuestionnaireResponse::class.java, questionnaireData)
        val strMap = scu.parse(map.path.getAbsolutePath(configPath).readFile(), map.name ?: "Main")
        scu.transform(contextR4, baseElement, strMap, targetResource)
        println("\n------End----------\n")

        return ParseResponse(
            file = FilenameUtils.getName(path),
            data = targetResource
        )
    }
}

private data class JsonConfig(
    val type: String,
    val map: MapConfig,
    val response: List<String>
)

data class ParseResponse(
    val file: String,
    val data: Bundle
)