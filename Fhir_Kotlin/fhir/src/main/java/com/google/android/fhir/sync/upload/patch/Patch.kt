package com.google.android.fhir.sync.upload.patch

import com.google.android.fhir.LocalChange
import com.google.android.fhir.LocalChangeToken
import java.time.Instant

/** Data class for squashed local changes for resource */
data class Patch(
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

    fun toLocalChange(): LocalChange = LocalChange(
        resourceType = resourceType,
        resourceId = resourceId,
        versionId = versionId,
        timestamp = timestamp,
        type =  LocalChange.Type.from(type.value),
        payload = payload,
        token = LocalChangeToken(listOf(0L))
    )
}

internal fun LocalChange.Type.toPatchType(): Patch.Type {
    return when (this) {
        LocalChange.Type.INSERT -> Patch.Type.INSERT
        LocalChange.Type.UPDATE -> Patch.Type.UPDATE
        LocalChange.Type.DELETE -> Patch.Type.DELETE
    }
}
