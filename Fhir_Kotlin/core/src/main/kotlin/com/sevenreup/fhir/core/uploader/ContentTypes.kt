package com.sevenreup.fhir.core.uploader

import okhttp3.MediaType.Companion.toMediaType

internal object ContentTypes {
    const val APPLICATION_JSON_PATCH = "application/json-patch+json"
    const val APPLICATION_FHIR_JSON = "application/fhir+json"
}

internal object MediaTypes {
    val MEDIA_TYPE_FHIR_JSON = ContentTypes.APPLICATION_FHIR_JSON.toMediaType()
}
