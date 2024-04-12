package mmap.database.users

class UserAuthFetchDTO(
    override val id: Int,
    override val email: String,
    val password: String,
): UsersFetchDTO(
    id = id,
    email = email
)