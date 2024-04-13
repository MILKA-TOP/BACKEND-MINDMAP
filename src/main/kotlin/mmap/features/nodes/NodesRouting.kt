package mmap.features.nodes

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import mmap.core.ApiResponse.Companion.respond
import mmap.plugins.authenticateRouting
import org.koin.ktor.ext.inject
import java.util.*

fun Application.configureNodesRouting() {
    val nodesModifyController by inject<NodesModifyController>()

    authenticateRouting {
        post("/nodes/toggle-selection{nodeId}") {
            val userId = call.principal<UserIdPrincipal>()?.name!!.toInt()
            val nodeId = UUID.fromString(call.parameters["nodeId"]!!)

            val response = nodesModifyController.toggleSelection(userId, nodeId)
            response.respond(call)
        }
    }
}
