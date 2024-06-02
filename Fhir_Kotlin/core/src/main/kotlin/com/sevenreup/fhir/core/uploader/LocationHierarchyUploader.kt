package com.sevenreup.fhir.core.uploader

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.google.gson.Gson
import com.sevenreup.fhir.core.config.AppConfig
import com.sevenreup.fhir.core.config.ProjectConfig
import com.sevenreup.fhir.core.config.ProjectConfigManager
import com.sevenreup.fhir.core.fhir.FhirConfigs
import com.sevenreup.fhir.core.utilities.TransformSupportServices
import com.sevenreup.fhir.core.utils.*
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Location
import org.hl7.fhir.r4.model.ResourceType
import org.hl7.fhir.r4.utils.StructureMapUtilities

class LocationHierarchyUploader(private val fhirServerUrl: String, private val fhirServerUrlApiKey: String) {
    private lateinit var projectConfig: ProjectConfig
    private lateinit var appConfig: AppConfig

    private val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    private val scu: StructureMapUtilities
    private var contextR4: SimpleWorkerContext = FhirConfigs.createWorkerContext()

    init {
        scu = StructureMapUtilities(contextR4, TransformSupportServices(contextR4))
    }


    fun upload(environment: String,directoryPath: String, projectRoot: String) {
        val configManager = ProjectConfigManager()
        projectConfig = configManager.loadProjectConfig(projectRoot, directoryPath)
        appConfig = configManager.loadUploaderConfigs(projectConfig, projectRoot)

        val envData = appConfig.getEnvironmentConfig(environment)
        getBundle(directoryPath)
    }

    fun getBundle(path: String) {
        val content = path.readFile()
        val bundle: Bundle = iParser.parseResource(Bundle::class.java, content)
        val roots = mutableListOf<LocationHierarchy>()
        val list = mutableListOf<Location>()
        for (entry in bundle.entry) {
            if (entry.resource.resourceType == ResourceType.Location) {
                val location = entry.resource as Location
                list.add(location)
            }
        }

       val loc = buildHierarchy(list)
        printHierarchy(loc)
        val gson = Gson()
        val json = gson.toJson(loc)
        val file = "${path.getParentPath()}/computed_location.json"
        json.createFile(file)
    }

    private fun buildHierarchy(locations: List<Location>): LocationHierarchy {
        val locationMap = locations.associateBy { it.logicalId }
        val hierarchyMap = mutableMapOf<String, MutableList<Location>>()

        locations.forEach { location ->
            var parentId = location.partOf?.extractId() ?: "root"
            if (parentId.isBlank()) {
                parentId = "root"
            }
            hierarchyMap.computeIfAbsent(parentId) { mutableListOf() }.add(location)
        }

        fun buildTree(parentId: String): List<LocationHierarchy> {
            return hierarchyMap[parentId]?.map { location ->
                LocationHierarchy(
                    identifier = location.logicalId,
                    name = location.name ?: "Unnamed",
                    children = buildTree(location.logicalId).toMutableList()
                )
            } ?: emptyList()
        }

        val rootHierarchy = buildTree("root")
        return if (rootHierarchy.size == 1) {
            rootHierarchy.first()
        } else {
            LocationHierarchy("root", "Root", rootHierarchy.toMutableList())
        }
    }

    private fun printHierarchy(locationHierarchy: LocationHierarchy, level: Int = 0) {
        val indent = "  ".repeat(level)
        println("$indent- ${locationHierarchy.name} (ID: ${locationHierarchy.identifier})")
        locationHierarchy.children.forEach { child ->
            printHierarchy(child, level + 1)
        }
    }
}

data class LocationHierarchy(
    val identifier: String,
    val name: String,
    val children: MutableList<LocationHierarchy> = mutableListOf(),
)