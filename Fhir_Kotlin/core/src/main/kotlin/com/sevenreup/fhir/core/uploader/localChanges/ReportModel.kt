package com.sevenreup.fhir.core.uploader.localChanges

import com.google.android.fhir.LocalChange

data class LocalChangesModel(
    val changes: List<LocalChange>,
    val size: Int,
    val type: String = "LocalChanges",
    val exception: Exception? = null
)

data class GeneralReportItem(
    val key: String,
    val values: Map<String, String>,
)

data class GeneralReport(
    val type: String,
    val items: List<GeneralReportItem>,
)

