package mmap.domain.auth

import mmap.data.auth.SessionsDataSource
import mmap.data.auth.UsersDataSource
import mmap.database.sessions.SessionDTO
import mmap.database.users.UserInsertDTO
import mmap.domain.auth.models.response.SessionResponseRemote
import mmap.extensions.salt

class AuthRepository(
    private val sessionDataSource: SessionsDataSource,
    private val usersDataSource: UsersDataSource,
) {

    fun createTemporarySession(userId: Int, deviceId: String, email: String): SessionResponseRemote {
        val sessionDTO = sessionDataSource.addDisabledSessionOrGetCreated(userId, deviceId)
        requireNotNull(sessionDTO) { "Error in activating sessions with this userId and deviceId" }
        return SessionResponseRemote(
            sessionId = sessionDTO.sessionId.toString(),
            userId = sessionDTO.userId.toString(),
            userEmail = email,
        )
    }

    fun selectUserById(userId: Int) = usersDataSource.selectById(userId)

    fun getSessionByUserIdAndDeviceId(userId: Int, deviceId: String): SessionDTO? =
        sessionDataSource.getSessionByUserIdAndDeviceId(userId, deviceId)
    fun revokeSession(userId: Int, deviceId: String) =
        sessionDataSource.revokeSession(userId, deviceId)

    fun disableSessions(userId: Int) {
        sessionDataSource.disableSessions(userId)
    }

    fun activateSession(userId: Int, deviceId: String, tokenSalt: String): SessionDTO =
        sessionDataSource.activateSession(userId, deviceId, tokenSalt)

    fun fetchUserByEmail(email: String) = usersDataSource.fetchByEmail(email)

    fun createUser(email: String, password: String): Int = usersDataSource.createUser(
        UserInsertDTO(
            email = email,
            password = password.salt()
        )
    )
}
