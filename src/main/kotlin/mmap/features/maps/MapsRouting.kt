package mmap.features.maps

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import mmap.features.maps.models.request.MapRemoveType
import mmap.features.maps.models.request.MapsMigrateRequestParams
import mmap.plugins.authenticateRouting

fun Application.configureMapsRouting() {

    authenticateRouting {
        post("/maps/create") {
            val userId = call.principal<UserIdPrincipal>()?.name!!

            val catalogController = MapsController(call)
            catalogController.createNewMap(userId)
        }
    }
    authenticateRouting {
        post("/maps/migrate") {
            val userId = call.principal<UserIdPrincipal>()?.name!!.toInt()
            val params = call.receive<MapsMigrateRequestParams>()

            val catalogController = MapsController(call)
            catalogController.migrate(userId, params)
        }
    }
    authenticateRouting {
        post("/maps/add-map") {
            val userId = call.principal<UserIdPrincipal>()?.name!!

            val catalogController = MapsController(call)
            catalogController.addNewMap(userId)
        }
    }
    authenticateRouting {
        get("/maps/fetch{mapId}") {
            val userId = call.principal<UserIdPrincipal>()?.name!!
            val mapId = call.parameters["mapId"]!!

            val mapsController = MapsController(call)
            mapsController.fetch(userId, mapId)
        }
    }
    authenticateRouting {
        get("/maps/view{mapId}{userId}") {
            val adminId = call.principal<UserIdPrincipal>()?.name!!
            val mapId = call.parameters["mapId"]!!
            val userId = call.parameters["userId"]!!

            val mapsController = MapsController(call)
            mapsController.fetch(requestUserId = adminId, mapId = mapId, fetchUserId = userId)
        }
    }
    authenticateRouting {
        post("/maps/update{mapId}") {
            val userId = call.principal<UserIdPrincipal>()?.name!!
            val mapId = call.parameters["mapId"]!!

            val mapsEditUpdateController = MapsEditUpdateController(call)
            mapsEditUpdateController.update(userId, mapId)
        }
    }
    authenticateRouting {
        post("/maps/erase{mapId}{type}") {
            val userId = call.principal<UserIdPrincipal>()?.name!!.toInt()
            val mapId = call.parameters["mapId"]!!.toInt()
            val removeType = MapRemoveType.valueOf(call.parameters["type"]!!)

            val mapsController = MapsController(call)
            mapsController.eraseInteractedMaps(mapId, userId, removeType)
        }
    }
    authenticateRouting {
        post("/maps/delete{mapId}") {
            val userId = call.principal<UserIdPrincipal>()?.name!!.toInt()
            val mapId = call.parameters["mapId"]!!.toInt()

            val mapsController = MapsController(call)
            mapsController.deleteEditableMap(mapId, userId)
        }
    }
}
