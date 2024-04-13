package mmap.features.maps

import mmap.core.ApiResponse
import mmap.database.maps.Maps
import mmap.domain.maps.MapsEditRepository
import mmap.domain.maps.models.request.MapsUpdateRequestParams
import mmap.extensions.AccessDenied

class MapsEditUpdateController(
    private val mapsEditRepository: MapsEditRepository
) {
    suspend fun update(userId: Int, mapId: Int, updatedParams: MapsUpdateRequestParams): ApiResponse<Any> {

        val mapAdminId = Maps.selectAdminId(mapId)

        if (mapAdminId != userId) return ApiResponse(AccessDenied, errorMessage = "You can't update this map")

        mapsEditRepository.update(mapId, updatedParams)

        return ApiResponse()
    }

}
