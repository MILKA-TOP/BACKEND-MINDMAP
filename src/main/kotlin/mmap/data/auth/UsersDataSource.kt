package mmap.data.auth

import mmap.database.users.UserAuthFetchDTO
import mmap.database.users.UserInsertDTO
import mmap.database.users.Users
import mmap.database.users.UsersFetchDTO

class UsersDataSource {

    fun selectById(userId: Int): UsersFetchDTO = Users.selectById(userId)
    fun fetchByEmail(email: String): UserAuthFetchDTO? = Users.fetchUser(email)
    fun createUser(userInsertDTO: UserInsertDTO): Int = Users.insert(userInsertDTO)
}
