package mmap.features.nodes

import io.ktor.http.*
import mmap.core.ApiResponse
import mmap.domain.nodes.NodesRepository
import mmap.domain.nodes.models.response.NodeToggleResponseRemote
import java.util.*

class NodesModifyController(private val nodesRepository: NodesRepository) {
    fun toggleSelection(userId: Int, nodeId: UUID): ApiResponse<NodeToggleResponseRemote> {
        val isNodeEnabled = nodesRepository.isEnabledInteractForUserByNodeId(nodeId, userId)
        if (!isNodeEnabled) return ApiResponse(
            HttpStatusCode.Conflict,
            errorMessage = "You doesn't have access for this node and map"
        )

        val toggleResult: Boolean = nodesRepository.toggleNode(nodeId, userId)

        return ApiResponse(
            data = NodeToggleResponseRemote(
                nodeId = nodeId.toString(),
                isMarked = toggleResult
            )
        )
    }
}
