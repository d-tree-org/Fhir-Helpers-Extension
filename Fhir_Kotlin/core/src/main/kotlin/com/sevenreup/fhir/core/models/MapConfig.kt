package com.sevenreup.fhir.core.models

import kotlinx.serialization.Serializable

@Serializable
data class MapConfig(
    val path: String = "",
    val name: String? = null
)