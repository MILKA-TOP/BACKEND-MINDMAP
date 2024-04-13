package mmap.domain.nodes

import mmap.data.maps.MapsDataSource
import mmap.data.maps.NodesDataSource
import java.util.*

class NodesRepository(
    private val mapsDataSource: MapsDataSource,
    private val nodesRepository: NodesDataSource,
) {

    fun isEnabledInteractForUserByNodeId(nodeId: UUID, userId: Int) =
        mapsDataSource.isEnabledInteractForUserByNodeId(nodeId, userId)

    fun toggleNode(nodeId: UUID, userId: Int): Boolean =
        nodesRepository.toggleNode(nodeId, userId)
}
