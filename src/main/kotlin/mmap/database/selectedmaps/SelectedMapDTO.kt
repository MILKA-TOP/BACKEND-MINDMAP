package mmap.database.selectedmaps

import mmap.database.users.UsersFetchDTO

data class SelectedMapDTO(
    val id: Int,
    val title: String,
    val description: String,
    val admin: UsersFetchDTO,
    val isSaved: Boolean,
    val referralId: String,
    val passwordHash: String?
)