package mmap.database.maps


data class CreateMapsDTO(
    val adminId: Int,
    val title: String,
    val description: String,
    val referralId: String,
    val passwordHash: String? = null,
)