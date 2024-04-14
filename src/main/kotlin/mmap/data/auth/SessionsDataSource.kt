package mmap.data.auth

import mmap.core.IgnoreCoverage
import mmap.database.sessions.SessionDTO
import mmap.database.sessions.Sessions

@IgnoreCoverage
class SessionsDataSource {

    fun addDisabledSessionOrGetCreated(userId: Int, deviceId: String): SessionDTO? =
        Sessions.addDisabledSessionOrGetCreated(userId, deviceId)

    fun getSessionByUserIdAndDeviceId(userId: Int, deviceId: String): SessionDTO? =
        Sessions.getSessionByUserIdAndDeviceId(userId, deviceId)

    fun disableSessions(userId: Int) {
        Sessions.disableSessions(userId)
    }

    fun activateSession(userId: Int, deviceId: String, tokenSalt: String) =
        Sessions.activateSession(userId, deviceId, tokenSalt)

    fun revokeSession(userId: Int, deviceId: String) {
        Sessions.revokeSession(userId, deviceId)
    }
}
