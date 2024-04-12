package mmap.features.auth.registry

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import mmap.database.users.UserAuthFetchDTO
import mmap.database.users.UserInsertDTO
import mmap.database.users.Users

class RegistryController(private val call: ApplicationCall) {

    suspend fun performRegistry(): UserAuthFetchDTO? {
        val receive = call.receive<RegistryReceiveRemote>()
        val userDTO = Users.fetchUser(receive.email)

        if (userDTO != null) {
            call.respond(HttpStatusCode.Conflict, "User already exists")
        } else {
            val insertUserId = Users.insert(
                UserInsertDTO(
                    email = receive.email,
                    password = receive.password
                )
            )
            return UserAuthFetchDTO(
                id = insertUserId,
                email = receive.email,
                password = receive.password
            )
        }
        return null
    }
}