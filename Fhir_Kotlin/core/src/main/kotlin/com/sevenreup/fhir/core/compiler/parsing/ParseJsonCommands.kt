package com.sevenreup.fhir.core.compiler.parsing

import ca.uhn.fhir.parser.IParser
import com.google.gson.Gson
import com.sevenreup.fhir.core.compiler.imports.handleImports
import com.sevenreup.fhir.core.config.ProjectConfig
import com.sevenreup.fhir.core.models.MapConfig
import com.sevenreup.fhir.core.utils.CoreResponse
import com.sevenreup.fhir.core.utils.getAbsolutePath
import com.sevenreup.fhir.core.utils.getParentPath
import com.sevenreup.fhir.core.utils.readFile
import org.apache.commons.io.FilenameUtils
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.StructureMap
import org.hl7.fhir.r4.utils.StructureMapUtilities

class ParseJsonCommands {
    val strCache: MutableMap<String, StructureMap> = mutableMapOf()

    fun parseSingle(
        path: String,
        iParser: IParser,
        scu: StructureMapUtilities,
        projectConfig: ProjectConfig
    ): CoreResponse<String> {
        val map = handleImports(path, iParser, scu, projectConfig)
        return CoreResponse(data = iParser.encodeResourceToString(map))
    }

    fun parseFromConfig(
        path: String,
        iParser: IParser,
        scu: StructureMapUtilities,
        contextR4: SimpleWorkerContext,
        projectConfig: ProjectConfig
    ): CoreResponse<Map<String, String>> {
        val rawJson = path.readFile()
        val gson = Gson()
        val config = gson.fromJson(rawJson, JsonConfig::class.java)

        val data = config.response.toHashSet().associate { res ->
            val bundle = parseBundle(iParser, contextR4, scu, path.getParentPath(), config.map, res, projectConfig)
            FilenameUtils.getName(bundle.file) to iParser.encodeResourceToString(bundle.data)
        }

        return CoreResponse(data = data)
    }

    fun parseBundle(
        iParser: IParser,
        contextR4: SimpleWorkerContext,
        scu: StructureMapUtilities,
        parentPath: String,
        map: MapConfig,
        path: String,
        projectConfig: ProjectConfig
    ): ParseResponse {
        println("\n----Start {${FilenameUtils.getName(path)}}-----")
        val questionnaireData = path.getAbsolutePath(parentPath).readFile()
        val targetResource = Bundle()
        val strMap: StructureMap?
        val baseElement =
            iParser.parseResource(QuestionnaireResponse::class.java, questionnaireData)
        if (strCache.containsKey(map.path)) {
            strMap = strCache[map.path]
        } else {
            strMap = handleImports(map.path.getAbsolutePath(parentPath), iParser, scu, projectConfig)
            strMap?.also { strCache[map.path] = it }
        }

        scu.transform(contextR4, baseElement, strMap, targetResource)
        println("\n------End----------\n")

        return ParseResponse(
            file = FilenameUtils.getName(path),
            data = targetResource
        )
    }

    companion object {
        fun getSrcName(path: String): String {
            return "Name"
        }
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