package com.sevenreup.fhir.core.uploader.localChanges

import org.hl7.fhir.r4.model.Resource
import java.time.Instant

data class LocalChangesModel(
    val changes: List<LocalChange>,
    val size: Int,
    val type: String,
)

data class GeneralReportItem(
    val key: String,
    val values: Map<String, String>,
)

data class GeneralReport(
    val type: String,
    val items: List<GeneralReportItem>,
)


// Note: Copied from com/google/android/fhir/LocalChange.kt
data class LocalChange(
    /** The [ResourceType] */
    val resourceType: String,
    /** The resource id [Resource.id] */
    val resourceId: String,
    /** This is the id of the version of the resource that this local change is based of */
    val versionId: String? = null,
    /** The time instant the app user performed a CUD operation on the resource. */
    val timestamp: Instant,
    /** Type of local change like insert, delete, etc */
    val type: Type,
    /** json string with local changes */
    val payload: String,
    /**
     * This token value must be explicitly applied when list of local changes are squashed and
     * [LocalChange] class instance is created.
     */
    var token: LocalChangeToken,
) {
    enum class Type(val value: Int) {
        INSERT(1), // create a new resource. payload is the entire resource json.
        UPDATE(2), // patch. payload is the json patch.
        DELETE(3), // delete. payload is empty string.
        ;

        companion object {
            fun from(input: Int): Type = values().first { it.value == input }
        }
    }
}

data class LocalChangeToken(val ids: List<Long>)

