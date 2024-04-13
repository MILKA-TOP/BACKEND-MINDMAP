package mmap.features.nodes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import mmap.database.nodeprogress.NodeProgress
import mmap.database.selectedmaps.SelectedMaps
import mmap.features.nodes.models.response.NodeToggleResponseRemote
import java.util.*

class NodesModifyController(private val call: ApplicationCall) {
    suspend fun toggleSelection(userId: String, nodeId: String) {
        val userIdInt = userId.toInt()
        val nodeIdUuid = UUID.fromString(nodeId)

        val isNodeEnabled = SelectedMaps.isEnabledInteractForUserByNodeId(nodeIdUuid, userIdInt)

        if (isNodeEnabled) {
            val toggleResult: Boolean = NodeProgress.toggleNode(nodeIdUuid, userIdInt)
            call.respond(
                HttpStatusCode.OK, NodeToggleResponseRemote(
                    nodeId = nodeId,
                    isMarked = toggleResult
                )
            )
        } else {
            call.respond(HttpStatusCode.Conflict, "You doesn't have access for this node and map")
        }
    }
}
