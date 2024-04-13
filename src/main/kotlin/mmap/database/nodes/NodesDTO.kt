package mmap.database.nodes

import java.util.UUID

data class NodesDTO(
    val id: UUID,
    val mapId: Int,
    val label: String,
    val description: String?,
    val parentNodeId: UUID?,
    val priorityPosition: Int,
)
