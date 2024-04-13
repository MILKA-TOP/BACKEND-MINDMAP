package mmap.domain.maps.models.response

import kotlinx.serialization.Serializable
import mmap.database.users.UsersFetchDTO

@Serializable
data class UserResponseRemote(
    val id: String,
    val email: String,
) {
    companion object {
        fun UsersFetchDTO.toDomainModel() = UserResponseRemote(
            id = id.toString(),
            email = email,
        )
    }
}
