package com.sevenreup.fhir.core.uploader.localChanges

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.google.android.fhir.LocalChange
import com.google.android.fhir.sync.upload.patch.PatchOrdering
import com.google.android.fhir.sync.upload.patch.PatchOrdering.sccOrderByReferences
import com.google.gson.Gson
import com.sevenreup.fhir.core.config.ProjectConfig
import com.sevenreup.fhir.core.config.ProjectConfigManager
import com.sevenreup.fhir.core.fhir.FhirConfigs
import com.sevenreup.fhir.core.fhir.FhirResourceHelper
import com.sevenreup.fhir.core.uploader.ContentTypes
import com.sevenreup.fhir.core.uploader.general.FhirUploader
import com.sevenreup.fhir.core.utilities.TransformSupportServices
import com.sevenreup.fhir.core.utils.*
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Binary
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.utils.StructureMapUtilities
import kotlin.io.path.Path

class LocalChangesUploader(private val batchSize: Int = 10) {
    private val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    private val scu: StructureMapUtilities
    private var contextR4: SimpleWorkerContext = FhirConfigs.createWorkerContext()
    private lateinit var projectConfig: ProjectConfig
    private lateinit var dotenv: Dotenv
    private lateinit var uploader: FhirUploader
    val gson: Gson
    private var currentDir = ""
    private val resourceHelper = FhirResourceHelper()

    init {
        scu = StructureMapUtilities(contextR4, TransformSupportServices(contextR4))
        gson = createGson()
    }

    fun work(filePath: String, projectRoot: String) {
        runBlocking {
            dotenv = dotenv {
                directory = projectRoot
            }
            currentDir = filePath.getParentPath()
            val configManager = ProjectConfigManager()
            projectConfig = configManager.loadProjectConfig(projectRoot, filePath)
            uploader = FhirUploader(dotenv, iParser)
            val data = filePath.readFile()
            val localChanges = gson.fromJson(data, LocalChangesModel::class.java)
            if (localChanges.changes.isNotEmpty()) {
                fixLocalChanges(localChanges.changes)
            }
        }
    }

    private suspend fun fixLocalChanges(localChanges: List<LocalChange>) {
        val groups = mutableMapOf<LocalChange.Type, List<LocalChange>>()
        for (change in localChanges) {
            if (groups.containsKey(change.type)) {
                val newList = mutableListOf(change)
                newList.addAll(groups[change.type] ?: listOf())
                groups[change.type] = newList
            } else {
                groups[change.type] = listOf(change)
            }
        }

        if (groups.containsKey(LocalChange.Type.INSERT)) {
            groups[LocalChange.Type.INSERT] = handleCreateResources(groups[LocalChange.Type.INSERT] ?: listOf())
        } else if(groups.containsKey(LocalChange.Type.UPDATE)) {
            groups[LocalChange.Type.UPDATE] = resourceHelper.generateSquashedChangesMapping(groups[LocalChange.Type.UPDATE] ?: listOf())
        }

        val entries = groups.entries.sortedBy { it.key }.toList()
        for ((index, group) in entries.withIndex()) {
            try {
                uploadChanges(group.key, group.value)
            } catch (e: Exception) {
                Logger.error(e)
                saveFailed(e,entries.subList(index, entries.size))
                throw e
            }
        }
        Logger.info("Finished upload, clearing log data")
        clearErrorFile()
    }

    private fun handleCreateResources(changes: List<LocalChange>): List<LocalChange> {
      val refs = changes.flatMap {
          val resource =  iParser.parseResource(it.payload) as Resource
          resourceHelper.extractLocalChangeWithRefs(it,resource)
        }
       val ordered = refs.sccOrderByReferences()
        return ordered.flatMap { it.patchMappings.map { it.localChange } }
    }

    private fun saveFailed(exception: java.lang.Exception, changes: List<Map.Entry<LocalChange.Type, List<LocalChange>>>) {
        val items = changes.flatMap { it.value }
        val data = ChangeErrorData(changes = items, exception = exception)
        val json = gson.toJson(data)
        json.createFile(Path(currentDir).resolve("upload-errors.json").toString())
    }

    private fun clearErrorFile() {
       saveFailed(Exception(""), listOf())
    }

    private suspend fun uploadChanges(changeType: LocalChange.Type, changes: List<LocalChange>) {
        val bundleEntries = if (changeType == LocalChange.Type.UPDATE) {
            changes.map { patch ->
                createRequest(patch, createPathRequest(patch))
            }
        } else {
            changes.sortedBy { it.versionId }.map { change ->
                val resourceToUpload = iParser.parseResource(change.payload) as Resource
                createRequest(change, resourceToUpload)
            }
        }

        uploader.bundleUpload(bundleEntries, batchSize)
    }

    private fun createPathRequest(patch: LocalChange): Binary {
        return Binary().apply {
            contentType = ContentTypes.APPLICATION_JSON_PATCH
            data = patch.payload.toByteArray()
        }
    }

    private fun createRequest(change: LocalChange, resourceToUpload: Resource): BundleEntryComponent {
        return BundleEntryComponent().apply {
            resource = resourceToUpload
            request = Bundle.BundleEntryRequestComponent().apply {
                url = "${change.resourceType}/${change.resourceId}"
                method = when (change.type) {
                    LocalChange.Type.INSERT -> Bundle.HTTPVerb.PUT
                    LocalChange.Type.UPDATE -> Bundle.HTTPVerb.PATCH
                    LocalChange.Type.DELETE -> Bundle.HTTPVerb.DELETE
                }
            }
        }
    }
}

data class ChangeErrorData(
     val changes: List<LocalChange>,
     val exception: Exception
)