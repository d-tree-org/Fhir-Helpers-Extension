package com.sevenreup.fhir.core.config

data class AppConfig(
    val environments:Environments,
    val configs: List<AppConfigSetup> = listOf()
)

data class Environments(
    val development: EnvironmentData,
    val staging: EnvironmentData,
    val production: EnvironmentData,
)

data class EnvironmentData(
    val titleAppend: String = "",
    val binaryAppend: String = "",
)

data class AppConfigSetup(
    val main: String,
    val composition: String,
    val variables: Map<String, String> = mapOf()
)