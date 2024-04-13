package mmap.features.catalog

import kotlinx.serialization.Serializable
import mmap.features.maps.models.response.UserResponseRemote

@Serializable
data class MapCatalogRemote(
    val id: String,
    val title: String,
    val admin: UserResponseRemote,
    val description: String,
    val isEnableEdit: Boolean,
    val isSaved: Boolean,
    val isPrivate: Boolean
)
