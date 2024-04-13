package mmap.features.maps

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import mmap.core.ApiResponse.Companion.respond
import mmap.domain.maps.models.request.*
import mmap.plugins.authenticateRouting
import org.koin.ktor.ext.inject

fun Application.configureMapsRouting() {

    val mapsController: MapsController by inject<MapsController>()
    val mapsEditController: MapsEditUpdateController by inject<MapsEditUpdateController>()

    authenticateRouting {
        post("/maps/create") {
            val userId = call.principal<UserIdPrincipal>()?.name!!.toInt()
            val createParams = call.receive<MapsCreateRequestParams>()

            mapsController.createNewMap(userId, createParams)
        }
    }
    authenticateRouting {
        post("/maps/migrate") {
            val userId = call.principal<UserIdPrincipal>()?.name!!.toInt()
            val params = call.receive<MapsMigrateRequestParams>()

            mapsController.migrate(userId, params)
        }
    }
    authenticateRouting {
        post("/maps/add-map") {
            val userId = call.principal<UserIdPrincipal>()?.name!!.toInt()
            val addParams = call.receive<MapsAddRequestParams>()

            val response = mapsController.addNewMap(userId, addParams)
            response.respond(call)
        }
    }
    authenticateRouting {
        get("/maps/fetch{mapId}") {
            val userId = call.principal<UserIdPrincipal>()?.name!!.toInt()
            val mapId = call.parameters["mapId"]!!.toInt()

            val response = mapsController.fetch(userId, mapId)
            response.respond(call)
        }
    }
    authenticateRouting {
        get("/maps/view{mapId}{userId}") {
            val adminId = call.principal<UserIdPrincipal>()?.name!!.toInt()
            val mapId = call.parameters["mapId"]!!.toInt()
            val userId = call.parameters["userId"]!!.toInt()

            val response = mapsController.fetch(requestUserId = adminId, mapId = mapId, fetchUserId = userId)
            response.respond(call)
        }
    }
    authenticateRouting {
        post("/maps/update{mapId}") {
            val userId = call.principal<UserIdPrincipal>()?.name!!.toInt()
            val mapId = call.parameters["mapId"]!!.toInt()
            val updateParams = call.receive<MapsUpdateRequestParams>()

            val response = mapsEditController.update(userId, mapId, updateParams)
            response.respond(call)
        }
    }
    authenticateRouting {
        post("/maps/erase{mapId}{type}") {
            val userId = call.principal<UserIdPrincipal>()?.name!!.toInt()
            val mapId = call.parameters["mapId"]!!.toInt()
            val removeType = MapRemoveType.valueOf(call.parameters["type"]!!)

            val response = mapsController.eraseInteractedMaps(mapId, userId, removeType)
            response.respond(call)
        }
    }
    authenticateRouting {
        post("/maps/delete{mapId}") {
            val userId = call.principal<UserIdPrincipal>()?.name!!.toInt()
            val mapId = call.parameters["mapId"]!!.toInt()

            val response = mapsController.deleteEditableMap(mapId, userId)
            response.respond(call)
        }
    }
}
