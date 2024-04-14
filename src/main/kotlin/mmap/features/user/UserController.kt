package mmap.features.user

import io.ktor.http.*
import io.ktor.server.auth.*
import mmap.core.ApiResponse
import mmap.database.sessions.SessionDTO
import mmap.database.sessions.Sessions
import mmap.domain.auth.AuthRepository
import mmap.domain.auth.models.request.EnterDataReceiveRemote
import mmap.domain.auth.models.request.RegistryReceiveRemote
import mmap.domain.auth.models.response.SessionResponseRemote
import mmap.extensions.salt
import org.apache.hc.core5.util.TextUtils

class UserController(
    private val authRepository: AuthRepository,
) {
    fun tryConnectUser(userId: Int, deviceId: String, token: String): SessionResponseRemote {
        val userDTO = authRepository.selectUserById(userId)
        val sessionDTO = authRepository.getSessionByUserIdAndDeviceId(userId, deviceId)
        requireNotNull(sessionDTO)
        val tokenSalt = token.salt()

        require(tokenSalt == sessionDTO.tokenSalt) { "Incorrect pin input" }

        val updatedSessionDTO = connectUser(userId, deviceId, token)

        return SessionResponseRemote(
            sessionId = updatedSessionDTO.sessionId.toString(),
            userId = updatedSessionDTO.userId.toString(),
            userEmail = userDTO.email,
        )
    }

    fun generateToken(userId: Int, deviceId: String): String {
        require(deviceId.isNotEmpty()) { "DeviceId is empty." }
        val dataToHash = "$userId-$deviceId".salt()
        return dataToHash.slice(0 until 16)
    }

    fun connectUser(userId: Int, deviceId: String, token: String): SessionDTO {
        authRepository.disableSessions(userId)
        return authRepository.activateSession(userId, deviceId, token.salt())
    }

    fun tryRevokeDevice(userId: Int, deviceId: String) {
        val userDTO = runCatching { authRepository.selectUserById(userId) }.getOrNull()
        if (userDTO != null) {
            val sessionDTO = authRepository.getSessionByUserIdAndDeviceId(userId, deviceId)
            requireNotNull(sessionDTO) { "Unknown session with this device" }

            authRepository.revokeSession(userId, deviceId)
        }
    }

    fun performRegistry(registryData: RegistryReceiveRemote, deviceId: String): SessionResponseRemote {
        val email = registryData.email.trim()
        val password = registryData.password.trim()
        val userDTO = authRepository.fetchUserByEmail(registryData.email)
        require(userDTO == null) { "User already exists" }
        require(email.isEmailValid()) { "Email is incorrect" }
        require(password.length >= 8) { "Password input is incorrect" }

        val insertUserId = authRepository.createUser(email, password)
        return authRepository.createTemporarySession(insertUserId, deviceId, email)
    }

    fun performLogin(enterData: EnterDataReceiveRemote, deviceId: String): ApiResponse<SessionResponseRemote> {
        val userDTO = authRepository.fetchUserByEmail(enterData.email)
            ?: return ApiResponse(statusCode = HttpStatusCode.BadRequest, errorMessage = "User not found")

        val inputPasswordSalt = enterData.password.salt()
        if (userDTO.password != inputPasswordSalt) {
            return ApiResponse(statusCode = HttpStatusCode.BadRequest, errorMessage = "Invalid password")
        }
        val session = authRepository.createTemporarySession(userDTO.id, deviceId, enterData.email)
        return ApiResponse(data = session)
    }

    companion object {
        private const val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"

        fun String.isEmailValid(): Boolean {
            return !TextUtils.isEmpty(this) && matches(emailRegex.toRegex())
        }

        fun getActiveUserIdByToken(token: BearerTokenCredential): Int? = Sessions.selectActiveByToken(token.token)
        fun getTemporaryUserIdByToken(token: BearerTokenCredential): Int? = Sessions.selectTemporaryByToken(token.token)
    }
}
