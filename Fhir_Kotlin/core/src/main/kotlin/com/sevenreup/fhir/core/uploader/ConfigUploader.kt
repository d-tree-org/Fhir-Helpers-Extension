package com.sevenreup.fhir.core.uploader

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.sevenreup.fhir.core.config.*
import com.sevenreup.fhir.core.fhir.FhirConfigs
import com.sevenreup.fhir.core.utilities.TransformSupportServices
import com.sevenreup.fhir.core.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.utils.StructureMapUtilities

class ConfigUploader(private val fhirServerUrl: String, private val fhirServerUrlApiKey: String) {
    private lateinit var projectConfig: ProjectConfig
    private lateinit var appConfig: AppConfig

    private val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    private val scu: StructureMapUtilities
    private var contextR4: SimpleWorkerContext = FhirConfigs.createWorkerContext()

    init {
        scu = StructureMapUtilities(contextR4, TransformSupportServices(contextR4))
    }

    suspend fun upload(environment: String, directoryPath: String, projectRoot: String) {
        val client = OkHttpClient()
        val configManager = ProjectConfigManager()
        projectConfig = configManager.loadProjectConfig(projectRoot, directoryPath)
        appConfig = configManager.loadUploaderConfigs(projectConfig, projectRoot)

        val envData = appConfig.getEnvironmentConfig(environment)

        appConfig.configs.forEach { config ->
            uploadConfig(client, environment, config, envData, projectRoot)
        }
    }

    private suspend fun uploadConfig(
        client: OkHttpClient,
        environment: String,
        config: AppConfigSetup,
        envData: EnvironmentData,
        projectRoot: String,
        delayMillis: Long = 1000
    ) {
        try {
            val variables = combineValues(config, envData)
            val compositionRaw = config.composition.getAbsolutePath(projectRoot).readFile().replaceTemplate(variables)
            val currentPath = config.composition.getParentPath()

            val composition = iParser.parseResource(Composition::class.java, compositionRaw)
            val resources = mutableListOf<Resource>()

            composition.section = composition.section.map { section ->
                val path = section.focus.reference
                val id = path.getFileNameWithoutExtension()
                val actualRef = "${config.variables.getOrDefault("binaryPrepend", "")}$id${envData.binaryAppend}"

                val binary = handleBinary(
                    path.getAbsolutePath("${currentPath}/binaries/".getAbsolutePath(projectRoot)),
                    actualRef,
                    variables
                )
                resources.add(binary)

                section.focus = Reference().apply { reference = "${ResourceType.Binary.name}/${actualRef}" }
                section
            }

            val combinedId = if (config.appendEnv) "${appConfig.baseAppId}${envData.binaryAppend}" else variables["appId"] ?: ""
            val combinedConfig = getConfigBinary(combinedId, variables, currentPath)

            resources.add(composition)
            resources.add(combinedConfig)

            withContext(Dispatchers.IO) {
                try {
                    val response = Uploader.upload(client, iParser, fhirServerUrl, fhirServerUrlApiKey) {
                        resources.forEach { resource ->
                            addEntry(Uploader.createBundleEntry(resource))
                        }
                    }
                    if (!response.isSuccessful) {
                        Logger.error("Failed to upload, env: ${environment}, comp: ${config.composition}: ${response.code} - ${response.message.ifEmpty { response.body?.string() }}")
                    } else {
                        Logger.info("Batch env: ${environment}, comp: ${config.composition} uploaded successfully")
                    }
                    response.close()
                } catch (e: Exception) {
                    Logger.error("Failed to upload batch env: ${environment}, comp: ${config.composition}: ${e.message}")
                }
            }
            delay(delayMillis)
        } catch (e: Exception) {
            Logger.error(e.toString())
        }
    }

    private fun getConfigBinary(appId: String, variables: Map<String, String>, currentPath: String): Binary {
        return handleBinary(
            "./config.json".getAbsolutePath(currentPath),
            appId,
            variables
        )
    }

    private fun handleBinary(path: String, resId: String, variables: Map<String, String>): Binary {
        val raw = path.readFile().replaceTemplate(variables)

        return Binary().apply {
            contentType = "application/json"
            id = resId
            data = raw.toByteArray()
        }
    }

    private fun combineValues(config: AppConfigSetup, envData: EnvironmentData): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map.putAll(config.variables)
        map.putAll(listOf(Pair("binaryAppend", envData.binaryAppend), Pair("titleAppend", envData.titleAppend)))
        return map
    }
}