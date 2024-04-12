package mmap.features.auth.sessions

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import mmap.database.sessions.SessionDTO
import mmap.database.sessions.Sessions
import mmap.database.users.UserAuthFetchDTO
import mmap.database.users.Users
import mmap.extensions.deviceId
import mmap.extensions.salt
import mmap.features.auth.SessionResponseRemote

class SessionController(private val call: ApplicationCall) {

    suspend fun temporaryConnectUser(userFetchDTO: UserAuthFetchDTO) {
        val deviceId = call.request.deviceId!!.trim()
        val sessionDTO = createTemporarySession(userFetchDTO.id, deviceId)
        call.respond(
            SessionResponseRemote(
                sessionId = sessionDTO.sessionId.toString(),
                userId = sessionDTO.userId.toString(),
                userEmail = userFetchDTO.email,
            )
        )
    }

    suspend fun tryConnectUser(userId: Int, deviceId: String, token: String) {
        val userDTO = Users.selectById(userId)
        val sessionDTO = Sessions.getSessionByUserIdAndDeviceId(userId, deviceId)
        requireNotNull(sessionDTO)
        val tokenSalt = token.salt()

        require(tokenSalt == sessionDTO.tokenSalt, { "Incorrect pin input" })

        disableActiveSessions(userId)
        val updatedSessionDTO = activateSession(userId, deviceId, token)

        call.respond(
            HttpStatusCode.OK,
            SessionResponseRemote(
                sessionId = updatedSessionDTO.sessionId.toString(),
                userId = updatedSessionDTO.userId.toString(),
                userEmail = userDTO.email,
            )
        )
    }

    suspend fun connectUser(userId: Int, deviceId: String, token: String) {
        val userDTO = Users.selectById(userId)
        disableActiveSessions(userId)
        val sessionDTO = activateSession(userId, deviceId, token)
    }

    private fun disableActiveSessions(userId: Int) {
        Sessions.disableSessions(userId)
    }

    private fun activateSession(userId: Int, deviceId: String, token: String): SessionDTO {
        val tokenSalt = token.salt()
        val sessionDTO = Sessions.activateSession(userId, deviceId, tokenSalt)

        return sessionDTO
    }


    private fun createTemporarySession(userId: Int, deviceId: String): SessionDTO {
        val sessionDTO = Sessions.addDisabledSessionOrGetCreated(userId, deviceId)
        requireNotNull(sessionDTO, { "Error in activating sessions with this userId and deviceId" })
        return sessionDTO
    }

    suspend fun tryRevokeDevice(userId: Int, deviceId: String) {
        val userDTO = runCatching { Users.selectById(userId) }.getOrNull()
        if (userDTO != null) {
            val sessionDTO = Sessions.getSessionByUserIdAndDeviceId(userId, deviceId)
            requireNotNull(sessionDTO, { "Unknown session with this device" })

            Sessions.revokeSession(userId, deviceId)
        }
        call.respond(HttpStatusCode.OK)
    }

    companion object {
        fun getActiveUserIdByToken(token: BearerTokenCredential): Int? = Sessions.selectActiveByToken(token.token)
        fun getTemporaryUserIdByToken(token: BearerTokenCredential): Int? = Sessions.selectTemporaryByToken(token.token)
    }
}