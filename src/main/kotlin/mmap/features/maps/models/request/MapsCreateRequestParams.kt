package mmap.features.maps.models.request

import kotlinx.serialization.Serializable

@Serializable
data class MapsCreateRequestParams(
    val title: String,
    val description: String,
    val password: String? = null,
    val ref: String? = null,
)