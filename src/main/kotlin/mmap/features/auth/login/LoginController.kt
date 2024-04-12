package mmap.features.auth.login

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import mmap.database.users.UserAuthFetchDTO
import mmap.database.users.Users
import mmap.extensions.salt
import mmap.features.auth.login.models.request.EnterDataReceiveRemote

class LoginController(private val call: ApplicationCall) {

    suspend fun performLogin(): UserAuthFetchDTO? {
        val receive = call.receive<EnterDataReceiveRemote>()
        val userDTO = Users.fetchUser(receive.email)

        if (userDTO == null) {
            call.respond(HttpStatusCode.BadRequest, "User not found")
        } else {
            if (userDTO.password != receive.password) {
                call.respond(HttpStatusCode.BadRequest, "Invalid password")
            }
        }
        return userDTO
    }

    fun generateToken(userId: Int, deviceId: String): String {
        require(deviceId.isNotEmpty(), { "DeviceId is empty." })
        val dataToHash = "${userId}-${deviceId}".salt()
        return dataToHash.slice(0 until 16)
    }

}