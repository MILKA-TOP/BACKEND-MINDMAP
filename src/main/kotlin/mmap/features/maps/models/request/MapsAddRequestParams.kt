package mmap.features.maps.models.request

import kotlinx.serialization.Serializable

@Serializable
data class MapsAddRequestParams(
    val mapId: String,
    val password: String? = null,
)
