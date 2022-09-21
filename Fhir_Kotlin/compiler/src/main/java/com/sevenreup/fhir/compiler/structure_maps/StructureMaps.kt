package com.sevenreup.fhir.compiler.structure_maps

import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.StructureMap
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager
import org.hl7.fhir.utilities.npm.ToolsVersion
import readFile

fun createStructureMapFromString(data: String, srcName: String): StructureMap? {
    val pcm = FilesystemPackageCacheManager(true, ToolsVersion.TOOLS_VERSION)
    // Package name manually checked from
    // https://simplifier.net/packages?Text=hl7.fhir.core&fhirVersion=All+FHIR+Versions
    val contextR4 = SimpleWorkerContext.fromPackage(pcm.loadPackage("hl7.fhir.r4.core", "4.0.1"))
    contextR4.isCanRunWithoutTerminology = true

    val scu = org.hl7.fhir.r4.utils.StructureMapUtilities(contextR4)
    return scu.parse(data, srcName)
}

fun createStructureMapFromFile(path: String, srcName: String): StructureMap? {
    val fileData = path.readFile()
    return createStructureMapFromString(fileData, srcName)
}