package com.sevenreup.fhir.core.models

import kotlinx.serialization.Serializable

@Serializable
data class MapConfig(
    val path: String = "",
    val name: String? = null,
    val defaultTests: List<DefaultTests> = listOf()
)

@Serializable
data class DefaultTests @JvmOverloads constructor(val type: String = "")