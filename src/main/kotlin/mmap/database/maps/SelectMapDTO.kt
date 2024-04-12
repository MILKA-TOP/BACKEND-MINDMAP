package mmap.database.maps

data class SelectMapDTO(
    val mapId: Int,
    val title: String,
    val description: String,
    val passwordHash: String?,
    val isRemoved: Boolean,
)