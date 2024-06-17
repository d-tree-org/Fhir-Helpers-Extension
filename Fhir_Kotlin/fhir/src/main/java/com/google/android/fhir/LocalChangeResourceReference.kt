package com.google.android.fhir

data class LocalChangeResourceReference(
    val resourceReferenceValue: String,
    val resourceReferencePath: String?,
    val localChangeId: Long,
    val localChange: LocalChange
)