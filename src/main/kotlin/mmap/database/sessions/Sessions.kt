package mmap.database.sessions

import mmap.database.users.Users
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object Sessions : UUIDTable(columnName = "session_id") {
    val userId = integer("user_id").references(Users.id)
    val deviceId = varchar("device_id", 255)
    val isActive = bool("is_active").default(false)
    val tokenSalt = varchar("token_salt", 255).nullable()

    fun selectActiveByToken(token: String): Int? = transaction {
        Sessions.select(userId)
            .where { (Sessions.id eq UUID.fromString(token)) and (isActive eq booleanParam(true)) }
            .singleOrNull()
            ?.get(userId)
    }

    fun selectTemporaryByToken(token: String): Int? = transaction {
        Sessions.select(userId)
            .where { (Sessions.id eq UUID.fromString(token)) }
            .singleOrNull()
            ?.get(userId)
    }

    fun disableSessions(userId: Int) = transaction {
        update({ Sessions.userId eq userId }) {
            it[isActive] = false
        }
    }

    fun getSessionByUserIdAndDeviceId(userId: Int, deviceId: String): SessionDTO? = try {
        transaction {
            val session = Sessions.selectAll()
                .where { (Sessions.userId eq userId) and (Sessions.deviceId eq deviceId) }
                .single()

            SessionDTO(
                sessionId = session[Sessions.id].value,
                userId = session[Sessions.userId],
                deviceId = session[Sessions.deviceId],
                tokenSalt = session[Sessions.tokenSalt]
            )
        }
    } catch (_: Exception) {
        null
    }

    fun activateSession(userId: Int, deviceId: String, tokenSalt: String): SessionDTO = transaction {
        Sessions.update(
            where = { (Sessions.userId eq userId) and (Sessions.deviceId eq deviceId) }
        ) {
            it[isActive] = true
            it[Sessions.tokenSalt] = tokenSalt
        }

        val session = Sessions.selectAll()
            .where { (Sessions.userId eq userId) and (Sessions.deviceId eq deviceId) }
            .single()

        SessionDTO(
            sessionId = session[Sessions.id].value,
            userId = session[Sessions.userId],
            deviceId = session[Sessions.deviceId],
            tokenSalt = session[Sessions.tokenSalt]
        )
    }

    fun addDisabledSessionOrGetCreated(userId: Int, deviceId: String): SessionDTO? = try {
        transaction {
            Sessions.insertIgnore {
                it[Sessions.userId] = userId
                it[Sessions.deviceId] = deviceId
            }

            Sessions.selectAll()
                .where { (Sessions.userId eq userId) and (Sessions.deviceId eq deviceId) }
                .single()
                .let { session ->
                    SessionDTO(
                        sessionId = session[Sessions.id].value,
                        userId = session[Sessions.userId],
                        deviceId = session[Sessions.deviceId],
                    )
                }
        }
    } catch (e: Exception) {
        null
    }

    fun revokeSession(userId: Int, deviceId: String) = transaction {
        update(
            where = { (Sessions.userId eq userId) and (Sessions.deviceId eq deviceId) }
        ) {
            it[tokenSalt] = null
            it[isActive] = false
        }
    }

}