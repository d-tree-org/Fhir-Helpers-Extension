package com.google.android.fhir.sync.upload.patch

import com.google.android.fhir.LocalChange
import com.google.android.fhir.LocalChangeResourceReference

internal typealias Node = String
internal typealias Graph = Map<Node, List<Node>>

object PatchOrdering {
    private val LocalChange.resourceTypeAndId: String
        get() = "${resourceType}/${resourceId}"

    fun List<LocalChangeResourceReference>.sccOrderByReferences(): List<StronglyConnectedPatchMappings> {
        val resourceIdToPatchMapping = associateBy { patchMapping -> patchMapping.localChange.resourceTypeAndId }
        /* Get LocalChangeResourceReferences for all the local changes. A single LocalChange may have
        multiple LocalChangeResourceReference, one for each resource reference in the
        LocalChange.payload.*/
        val localChangeIdToResourceReferenceMap: Map<Long, List<LocalChangeResourceReference>> = this.groupBy { it.localChangeId }

        val adjacencyList = createAdjacencyListForCreateReferences(localChangeIdToResourceReferenceMap)

        return StronglyConnectedPatches.scc(adjacencyList).map {
            StronglyConnectedPatchMappings(it.mapNotNull { resourceIdToPatchMapping[it] })
        }
    }

     fun List<LocalChangeResourceReference>.createAdjacencyListForCreateReferences(
        localChangeIdToReferenceMap: Map<Long, List<LocalChangeResourceReference>>,
    ): Map<Node, List<Node>> {
        val adjacencyList = mutableMapOf<Node, List<Node>>()
        /* if the outgoing reference is to a resource that's just an update and not create, then don't
        link to it. This may make the sub graphs smaller and also help avoid cyclic dependencies.*/
        val resourceIdsOfInsertTypeLocalChanges =
            asSequence()
                .filter { it.localChange.type == LocalChange.Type.INSERT }
                .map { it.localChange.resourceTypeAndId }
                .toSet()

        forEach { patchMapping ->
            adjacencyList[patchMapping.localChange.resourceTypeAndId] =
                patchMapping.localChange.findOutgoingReferences(localChangeIdToReferenceMap).filter {
                    resourceIdsOfInsertTypeLocalChanges.contains(it)
                }
        }
        return adjacencyList
    }

    private fun LocalChange.findOutgoingReferences(
        localChangeIdToReferenceMap: Map<Long, List<LocalChangeResourceReference>>,
    ): Set<Node> {
        val references = mutableSetOf<Node>()
        when (type) {
            LocalChange.Type.INSERT,
            LocalChange.Type.UPDATE, -> {
                token.ids.forEach { id ->
                    localChangeIdToReferenceMap[id]?.let {
                        references.addAll(it.map { it.resourceReferenceValue })
                    }
                }
            }
            LocalChange.Type.DELETE -> {
                // do nothing
            }
        }
        return references
    }
}