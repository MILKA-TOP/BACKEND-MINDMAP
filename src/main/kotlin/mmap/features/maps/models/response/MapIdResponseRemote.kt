package mmap.features.maps.models.response

import kotlinx.serialization.Serializable

@Serializable
data class MapIdResponseRemote(
    val mapId: String
)