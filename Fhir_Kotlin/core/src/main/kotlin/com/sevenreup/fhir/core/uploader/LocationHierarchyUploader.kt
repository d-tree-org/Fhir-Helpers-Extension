package com.sevenreup.fhir.core.uploader

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.google.gson.Gson
import com.sevenreup.fhir.core.fhir.FhirConfigs
import com.sevenreup.fhir.core.uploader.general.FhirClient
import com.sevenreup.fhir.core.utilities.TransformSupportServices
import com.sevenreup.fhir.core.utils.*
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Location
import org.hl7.fhir.r4.utils.StructureMapUtilities
import kotlin.io.path.Path

class LocationHierarchyUploader {
    private val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    private val scu: StructureMapUtilities
    private var contextR4: SimpleWorkerContext = FhirConfigs.createWorkerContext()

    private lateinit var dotenv: Dotenv
    private lateinit var fhirClient: FhirClient

    init {
        scu = StructureMapUtilities(contextR4, TransformSupportServices(contextR4))
    }


    fun createHierarchy(projectRoot: String, batchSize: Int) {
        runBlocking {
            dotenv = dotenv {
                directory = projectRoot
            }
            fhirClient = FhirClient(dotenv, iParser)
            val list = fhirClient.searchResources<Location>(count = batchSize) {

            }
            var loc = buildHierarchy(list)
            printHierarchy(loc)
            if (loc.children.size == 1) {
                loc = loc.children.first()
            }
            val gson = Gson()
            val json = gson.toJson(loc)
            val file = Path(projectRoot).resolve("out/locations/computed_location.json").toString()
            json.createFile(file)
        }
    }

    private fun buildHierarchy(locations: List<Location>): LocationHierarchy {
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