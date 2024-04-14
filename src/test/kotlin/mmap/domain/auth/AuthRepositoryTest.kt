package mmap.domain.auth

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import mmap.data.auth.SessionsDataSource
import mmap.data.auth.UsersDataSource
import mmap.database.sessions.SessionDTO
import mmap.database.users.UserAuthFetchDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class AuthRepositoryTest {

    private lateinit var sessionDataSource: SessionsDataSource
    private lateinit var usersDataSource: UsersDataSource
    private lateinit var authRepository: AuthRepository

    @BeforeTest
    fun setUp() {
        sessionDataSource = mockk(relaxed = true)
        usersDataSource = mockk(relaxed = true)
        authRepository = AuthRepository(sessionDataSource, usersDataSource)
    }

    @Test
    fun `createTemporarySession creates a session successfully`() {
        val userId = 1
        val deviceId = "device123"
        val email = "test@example.com"
        val sessionDTO = SessionDTO(UUID.randomUUID(), userId, "", "")

        every({ sessionDataSource.addDisabledSessionOrGetCreated(userId, deviceId) }).returns(sessionDTO)

        val result = authRepository.createTemporarySession(userId, deviceId, email)

        assertEquals(sessionDTO.sessionId.toString(), result.sessionId)
        assertEquals(userId.toString(), result.userId)
        assertEquals(email, result.userEmail)
    }

    @Test
    fun `createTemporarySession creates exception where session is null`() {
        val userId = 1
        val deviceId = "device123"
        val email = "test@example.com"

        every({ sessionDataSource.addDisabledSessionOrGetCreated(userId, deviceId) }).returns(null)

        assertThrows<IllegalArgumentException>() {
            authRepository.createTemporarySession(userId, deviceId, email)
        }

    }

    @Test
    fun `selectUserById selects a user successfully`() {
        val userId = 1
        val userDTO = UserAuthFetchDTO(userId, "test@example.com", "hashedPassword")

        every({ usersDataSource.selectById(userId) }).returns(userDTO)

        val result = authRepository.selectUserById(userId)

        assertEquals(userDTO, result)
    }

    @Test
    fun `getSessionByUserIdAndDeviceId fetches a session successfully`() {
        val userId = 1
        val deviceId = "device123"
        val sessionDTO = SessionDTO(UUID.randomUUID(), userId, "", "")

        every({ sessionDataSource.getSessionByUserIdAndDeviceId(userId, deviceId) }).returns(sessionDTO)

        val result = authRepository.getSessionByUserIdAndDeviceId(userId, deviceId)

        assertEquals(sessionDTO, result)
    }

    @Test
    fun `revokeSession revokes a session successfully`() {
        val userId = 1
        val deviceId = "device123"

        authRepository.revokeSession(userId, deviceId)

        verify(exactly = 1) {
            sessionDataSource.revokeSession(userId, deviceId)
        }
    }

    @Test
    fun `disableSessions disables sessions for a user successfully`() {
        val userId = 1

        authRepository.disableSessions(userId)

        verify(exactly = 1) {
            sessionDataSource.disableSessions(userId)
        }
    }

    @Test
    fun `activateSession activates a session successfully`() {
        val userId = 1
        val deviceId = "device123"
        val tokenSalt = "salt"
        val sessionDTO = SessionDTO(UUID.randomUUID(), userId, "", "")

        every({ sessionDataSource.activateSession(userId, deviceId, tokenSalt) }).returns(sessionDTO)

        val result = authRepository.activateSession(userId, deviceId, tokenSalt)

        assertEquals(sessionDTO, result)
    }

    @Test
    fun `fetchUserByEmail fetches a user by email successfully`() {
        val email = "test@example.com"
        val userDTO = UserAuthFetchDTO(1, email, "hashedPassword")

        every({ usersDataSource.fetchByEmail(email) }).returns(userDTO)

        val result = authRepository.fetchUserByEmail(email)

        assertEquals(userDTO, result)
    }

    @Test
    fun `createUser creates a user successfully`() {
        val email = "test@example.com"
        val password = "password"
        val userId = 1

        every { usersDataSource.createUser(any()) }.returns(userId)

        val result = authRepository.createUser(email, password)

        assertEquals(userId, result)
    }
}