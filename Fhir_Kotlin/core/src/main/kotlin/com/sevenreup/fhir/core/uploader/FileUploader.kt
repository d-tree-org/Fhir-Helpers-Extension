package com.sevenreup.fhir.core.uploader

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.sevenreup.fhir.core.compiler.parsing.ParseJsonCommands
import com.sevenreup.fhir.core.config.ProjectConfig
import com.sevenreup.fhir.core.config.ProjectConfigManager
import com.sevenreup.fhir.core.fhir.FhirConfigs
import com.sevenreup.fhir.core.uploader.general.DataResponseState
import com.sevenreup.fhir.core.uploader.general.FhirClient
import com.sevenreup.fhir.core.utilities.TransformSupportServices
import com.sevenreup.fhir.core.utils.Logger
import com.sevenreup.fhir.core.utils.readFile
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent
import org.hl7.fhir.r4.model.Parameters
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.utils.StructureMapUtilities
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager
import org.hl7.fhir.utilities.npm.ToolsVersion
import java.io.File
import java.io.IOException
import java.nio.file.Paths


class FileUploader() {
    private val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    private val scu: StructureMapUtilities
    private var contextR4: SimpleWorkerContext = FhirConfigs.createWorkerContext()
    private lateinit var projectConfig: ProjectConfig
    private val uploadList = mutableListOf<File>()
    private val excludeList = mutableSetOf<String>()

    private lateinit var dotenv: Dotenv
    private lateinit var fhirClient: FhirClient


    init {
        scu = StructureMapUtilities(contextR4, TransformSupportServices(contextR4))
    }


    suspend fun batchUpload(directoryPath: String, projectRoot: String) {
        dotenv = dotenv {
            directory = projectRoot
        }
        fhirClient = FhirClient(dotenv, iParser)

        val configManager = ProjectConfigManager()
        projectConfig = configManager.loadProjectConfig(projectRoot, directoryPath)
        processExcludedPaths(projectRoot)

        fetchFiles(
            directoryPath, mapOf(
                Pair(projectConfig.structureMapLocation, "map"),
                Pair(projectConfig.questionnaireMapLocation, "json")
            )
        )
        Logger.info("Found ${uploadList.size} files")
        uploadToFhirServer(uploadList)
    }

    private fun fetchFiles(directoryPath: String, maps: Map<String, String>) {
        for (map in maps) {
            val path = Paths.get(directoryPath, map.key).toAbsolutePath()
            val baseDirectory = path.toFile()

            if (baseDirectory.exists() && baseDirectory.isDirectory) {
                processFiles(baseDirectory, map.value)
            } else {
                Logger.error("Directory does not exist or is not a directory")
            }
        }
    }

    private fun compileMapToJson(mapFile: File): Resource? {
        return ParseJsonCommands().parseStructureMap(mapFile.absolutePath, iParser, scu, projectConfig)
    }

    private fun compileQuestionnaire(file: File): Resource? {
        return try {
            return iParser.parseResource(Questionnaire::class.java, file.readFile())
        } catch (e: Exception) {
            Logger.error("Path: ${file.path} Error: $e")
            null
        }
    }

    private fun createBundleEntry(res: Resource): BundleEntryComponent {
        val resUrl = "${res.fhirType()}/${res.idElement?.idPart.orEmpty()}"
        return BundleEntryComponent().apply {
            resource = res
            fullUrl = resUrl
            request = Bundle.BundleEntryRequestComponent().apply {
                method = Bundle.HTTPVerb.PUT
                url = resUrl
            }
        }
    }

    private suspend fun uploadToFhirServer(files: List<File>, batchSize: Int = 10) {
        val totalBatches = if (files.size % batchSize == 0) files.size / batchSize else files.size / batchSize + 1

        for (batchIndex in 0 until totalBatches) {
            val start = batchIndex * batchSize
            val end = minOf((batchIndex + 1) * batchSize, files.size)
            val batchFiles = files.subList(start, end)

            uploadBatch(batchFiles, batchIndex, totalBatches)
        }
    }

    private suspend fun uploadBatch(
        batchFiles: List<File>,
        batchIndex: Int,
        totalBatches: Int,
        delayMillis: Long = 1000
    ) {
        val entries = mutableListOf<BundleEntryComponent>()

        for (file in batchFiles) {
            try {
                if (file.extension == "map") {
                    val map = compileMapToJson(file) ?: continue
                    entries.add(createBundleEntry(map))
                } else {
                    val quest = compileQuestionnaire(file) ?: continue
                    entries.add(createBundleEntry(quest))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val response = fhirClient.uploadBatchUpload(entries)
        if (response is DataResponseState.Error) {
            Logger.error("Failed to upload batch ${batchIndex + 1}/$totalBatches: ${response.exception}")
        } else {
            Logger.info("Batch ${batchIndex + 1}/$totalBatches uploaded successfully")
        }
        delay(delayMillis)
    }

    private fun processExcludedPaths(projectRoot: String) {
        for (exclude in projectConfig.uploadExclude) {
            excludeList.add(Paths.get(projectRoot, exclude).normalize().toAbsolutePath().toString())
        }
    }

    private fun processFiles(directory: File, extension: String) {
        val files = directory.listFiles() ?: return

        for (file in files) {
            if (file.isDirectory) {
                val isNotExcluded = !excludeList.contains(file.absolutePath)
                if (isNotExcluded) {
                    processFiles(file, extension)
                }
            } else {
                if (extension == file.extension) {
                    uploadList.add(file)
                }
            }
        }
    }
}