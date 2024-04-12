package mmap.features.nodes

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import mmap.plugins.authenticateRouting

fun Application.configureNodesRouting() {
    authenticateRouting {
        post("/nodes/toggle-selection{nodeId}") {
            val userId = call.principal<UserIdPrincipal>()?.name!!
            val nodeId = call.parameters["nodeId"]!!

            val nodesModifyController = NodesModifyController(call)
            nodesModifyController.toggleSelection(userId, nodeId)
        }
    }

}