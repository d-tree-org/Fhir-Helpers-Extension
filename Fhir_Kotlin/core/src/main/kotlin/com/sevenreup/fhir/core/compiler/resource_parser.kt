package com.sevenreup.fhir.core.compiler

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.sevenreup.fhir.core.parsing.ParseJsonCommands
import com.sevenreup.fhir.core.utilities.TransformSupportServices
import com.sevenreup.fhir.core.utils.CoreResponse
import com.sevenreup.fhir.core.utils.readFile
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Parameters
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager
import org.hl7.fhir.utilities.npm.ToolsVersion

class ResourceParser {
    private val scu: org.hl7.fhir.r4.utils.StructureMapUtilities
    private val iParser: IParser
    private val contextR4: SimpleWorkerContext
    init {
        val pcm = FilesystemPackageCacheManager(true, ToolsVersion.TOOLS_VERSION)
         contextR4 = SimpleWorkerContext.fromPackage(pcm.loadPackage("hl7.fhir.r4.core", "4.0.1"))
        contextR4.isCanRunWithoutTerminology = true
        contextR4.setExpansionProfile(Parameters())
        scu = org.hl7.fhir.r4.utils.StructureMapUtilities(contextR4, TransformSupportServices(contextR4))

        iParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    }

    fun parseStructureMapFromMap(path: String): CoreResponse<String> {
        val map = scu.parse(path.readFile(), getSrcName(path))
        return CoreResponse(data = iParser.encodeResourceToString(map))
    }

    fun parseTransformFromJson(path: String): CoreResponse<Map<String, String>> {
        val data = ParseJsonCommands.parse(path, iParser, scu, contextR4)
        if (data.error != null) throw data.error
        return CoreResponse(data = data.data!!)
    }


    private fun getSrcName(path: String): String {
        return "Name"
    }
}