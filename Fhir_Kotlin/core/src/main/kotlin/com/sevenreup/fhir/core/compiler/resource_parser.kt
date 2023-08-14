package com.sevenreup.fhir.core.compiler

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.sevenreup.fhir.core.compiler.parsing.ParseJsonCommands
import com.sevenreup.fhir.core.config.ProjectConfigManager
import com.sevenreup.fhir.core.utilities.TransformSupportServices
import com.sevenreup.fhir.core.utils.CoreResponse
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Parameters
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager
import org.hl7.fhir.utilities.npm.ToolsVersion

class ResourceParser(val configManager: ProjectConfigManager) {
    private val scu: org.hl7.fhir.r4.utils.StructureMapUtilities
    private val iParser: IParser
    private val contextR4: SimpleWorkerContext
    private val parserCommand: ParseJsonCommands

    init {
        val pcm = FilesystemPackageCacheManager(true, ToolsVersion.TOOLS_VERSION)
        contextR4 = SimpleWorkerContext.fromPackage(pcm.loadPackage("hl7.fhir.r4.core", "4.0.1"))
        contextR4.isCanRunWithoutTerminology = true
        contextR4.setExpansionProfile(Parameters())
        scu = org.hl7.fhir.r4.utils.StructureMapUtilities(contextR4, TransformSupportServices(contextR4))

        iParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
        parserCommand = ParseJsonCommands()
    }

    fun parseStructureMapFromMap(path: String, projectRoot: String?): CoreResponse<String> {
        val configs = configManager.loadProjectConfig(projectRoot, path)
        return parserCommand.parseSingle(path, iParser, scu, configs)
    }

    fun parseTransformFromJson(path: String, projectRoot: String?): CoreResponse<Map<String, String>> {
        val configs = configManager.loadProjectConfig(projectRoot, path)
        val data = parserCommand.parseFromConfig(path, iParser, scu, contextR4, configs)
        if (data.error != null) throw data.error
        return CoreResponse(data = data.data!!)
    }


}