package com.sevenreup.fhir.core.fhir

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.util.FhirTerser
import com.google.android.fhir.LocalChange
import com.google.android.fhir.LocalChangeResourceReference
import com.google.android.fhir.sync.upload.patch.Patch
import com.google.android.fhir.sync.upload.patch.PerResourcePatchGenerator
import org.hl7.fhir.r4.model.Resource

class FhirResourceHelper {
    private var fhirTerser: FhirTerser = FhirTerser(FhirContext.forCached(FhirVersionEnum.R4))

   private fun extractResourceReferences(resource: Resource) =
        fhirTerser.getAllResourceReferences(resource).toSet()

    fun extractLocalChangeWithRefs(change: LocalChange, resource: Resource): List<LocalChangeResourceReference> {
       return extractResourceReferences(resource).mapNotNull { resourceReferenceInfo ->
            if (resourceReferenceInfo.resourceReference.referenceElement.value != null) {
                LocalChangeResourceReference(
                    resourceReferencePath = resourceReferenceInfo.name,
                    resourceReferenceValue = resourceReferenceInfo.resourceReference.referenceElement.value,
                    localChangeId = change.token.ids.first(),
                    localChange = change
                )
            } else {
                null
            }
        }
    }

    fun generateSquashedChangesMapping(localChanges: List<LocalChange>): List<LocalChange> {
       return localChanges
            .groupBy { it.resourceType to it.resourceId }
            .values.mapNotNull { resourceLocalChanges ->
                PerResourcePatchGenerator.with().mergeLocalChangesForSingleResource(resourceLocalChanges)
            }.map {
                it.toLocalChange()
           }
    }
}