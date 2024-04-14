package mmap.features.maps

import io.ktor.http.*
import mmap.core.ApiResponse
import mmap.domain.maps.MapsRepository
import mmap.domain.maps.models.request.MapRemoveType
import mmap.domain.maps.models.request.MapsAddRequestParams
import mmap.domain.maps.models.request.MapsCreateRequestParams
import mmap.domain.maps.models.request.MapsMigrateRequestParams
import mmap.domain.maps.models.response.*
import mmap.extensions.AccessDenied
import mmap.extensions.salt

class MapsController(private val mapsRepository: MapsRepository) {

    fun createNewMap(userId: Int, crateParams: MapsCreateRequestParams): MapIdResponseRemote {
        val mapId = mapsRepository.createNewMap(userId, crateParams)
        return MapIdResponseRemote(mapId = mapId.toString())
    }

    fun migrate(userId: Int, params: MapsMigrateRequestParams): MapIdResponseRemote {
        val mapId = mapsRepository.migrate(userId, params)

        return MapIdResponseRemote(mapId = mapId.toString())
    }

    fun addNewMap(userId: Int, addParams: MapsAddRequestParams): ApiResponse<Any> {
        val mapId = addParams.mapId.toInt()

        val map = mapsRepository.selectMapPreview(mapId)

        if (map.passwordHash == null) {
            if (addParams.password != null) {
                return ApiResponse(HttpStatusCode.BadRequest, errorMessage = "Incorrect password parameter")
            } else {
                mapsRepository.insertSelectionNewMap(mapId, userId)
                return ApiResponse()
            }
        } else {
            val inputPassword = addParams.password?.salt()
            if (inputPassword != map.passwordHash) {
                return ApiResponse(HttpStatusCode.BadRequest, errorMessage = "Incorrect password parameter")
            } else {
                mapsRepository.insertSelectionNewMap(mapId, userId)
                return ApiResponse()
            }
        }
    }

    fun fetch(
        requestUserId: Int,
        mapId: Int,
        fetchUserId: Int = requestUserId
    ): ApiResponse<SummaryMapResponseRemote> {
        val selectedMap = mapsRepository.fetch(requestUserId, mapId, fetchUserId)
        return selectedMap?.let { ApiResponse(data = it) } ?: ApiResponse(
            AccessDenied,
            errorMessage = "You doesn't contains this map in your catalog"
        )
    }

    fun eraseInteractedMaps(mapId: Int, userId: Int, removeType: MapRemoveType): ApiResponse<Any> {
        val checkEnabledMapForUser = mapsRepository.isEnabledInteractForUserByMapId(mapId, userId)

        if (checkEnabledMapForUser) {
            mapsRepository.eraseInteractedMaps(mapId, userId, removeType)
            return ApiResponse()
        }
        return ApiResponse(HttpStatusCode.Conflict, errorMessage = "You doesn't have access for this node and map")
    }

    fun deleteEditableMap(mapId: Int, userId: Int): ApiResponse<Any> {
        val mapAdminId = mapsRepository.selectAdminId(mapId)

        if (mapAdminId == userId) {
            mapsRepository.deleteEditableMap(mapId)
            return ApiResponse()
        }
        return ApiResponse(HttpStatusCode.Conflict, errorMessage = "You doesn't have access for this node and map")
    }

    companion object {
        const val PASSWORD_MIN_SIZE = 8
        const val REFERRAL_ID_SIZE = 8
    }
}
