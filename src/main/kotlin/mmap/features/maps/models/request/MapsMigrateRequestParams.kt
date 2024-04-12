package mmap.features.maps.models.request

import kotlinx.serialization.Serializable

@Serializable
data class MapsMigrateRequestParams(
    val text: String,
    val type: MigrateType,
    val password: String? = null,
)
