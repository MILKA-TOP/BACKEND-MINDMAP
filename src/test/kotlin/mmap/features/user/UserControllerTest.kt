package mmap.features.user

import io.ktor.http.*
import io.mockk.*
import junit.framework.TestCase
import mmap.database.sessions.SessionDTO
import mmap.database.users.UserAuthFetchDTO
import mmap.database.users.UsersFetchDTO
import mmap.domain.auth.AuthRepository
import mmap.domain.auth.models.request.EnterDataReceiveRemote
import mmap.domain.auth.models.request.RegistryReceiveRemote
import mmap.domain.auth.models.response.SessionResponseRemote
import mmap.extensions.salt
import org.junit.jupiter.api.Assertions.assertThrows
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserControllerTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var userController: UserController

    @BeforeTest
    fun setUp() {
        authRepository = mockk(relaxed = true)
        userController = UserController(authRepository)
    }

    @Test
    fun `testTryRevokeDeviceWhenValidUserAndDeviceIdThenRevokeSession`() {
        val userId = 1
        val deviceId = "device1"
        val userDTO = UsersFetchDTO(userId, "email@example.com")
        val sessionDTO = SessionDTO(UUID.randomUUID(), userId, deviceId, "validToken1234".salt())

        every { authRepository.selectUserById(userId) } returns userDTO
        every { authRepository.getSessionByUserIdAndDeviceId(userId, deviceId) } returns sessionDTO

        userController.tryRevokeDevice(userId, deviceId)

        verify { authRepository.revokeSession(userId, deviceId) }
    }

    @Test
    fun `testTryRevokeDeviceWhenInvalidUserIdThenDoNotRevokeSession`() {
        val userId = 1
        val deviceId = "device1"

        every { authRepository.selectUserById(userId) } throws IllegalArgumentException()

        userController.tryRevokeDevice(userId, deviceId)

        verify(exactly = 0) { authRepository.revokeSession(any(), any()) }
    }

    @Test
    fun `testTryRevokeDeviceWhenValidUserIdInvalidDeviceIdThenThrowException`() {
        val userId = 1
        val deviceId = "device1"
        val userDTO = UsersFetchDTO(userId, "email@example.com")

        every { authRepository.selectUserById(userId) } returns userDTO
        every { authRepository.getSessionByUserIdAndDeviceId(userId, deviceId) } returns null

        assertThrows(IllegalArgumentException::class.java) {
            userController.tryRevokeDevice(userId, deviceId)
        }
    }

    @Test
    fun `testPerformRegistryWhenUserDoesNotExistThenReturnsSessionResponseRemote`() {
        val registryData = RegistryReceiveRemote("email@example.com", "password1234")
        val deviceId = "device1"
        val sessionResponse = SessionResponseRemote("1", "1", "email@example.com")

        every { authRepository.fetchUserByEmail(registryData.email) } returns null
        every { authRepository.createUser(any(), any()) } returns 1
        every { authRepository.createTemporarySession(any(), any(), any()) } returns sessionResponse

        val result = userController.performRegistry(registryData, deviceId)

        assertNotNull(result)
        assertEquals(result, sessionResponse)
    }

    @Test
    fun `testPerformRegistryWhenUserExistsThenThrowsException`() {
        val registryData = RegistryReceiveRemote("email@example.com", "password1234")
        val deviceId = "device1"
        val sessionId = UUID.randomUUID()
        val userDTO = UserAuthFetchDTO(1, "email@example.com", "")

        mockkStatic(UUID::class)
        coEvery { UUID.randomUUID() } returns sessionId

        every { authRepository.fetchUserByEmail(registryData.email) } returns userDTO

        assertThrows(IllegalArgumentException::class.java) {
            userController.performRegistry(registryData, deviceId)
        }
    }

    @Test
    fun `testPerformRegistryWhenEmailIsInvalidThenThrowsException`() {
        val registryData = RegistryReceiveRemote("invalidEmail", "password1234")
        val deviceId = "device1"

        assertThrows(IllegalArgumentException::class.java) {
            userController.performRegistry(registryData, deviceId)
        }
    }

    @Test
    fun `testPerformRegistryWhenEmailIsEmptyThenThrowsException`() {
        val registryData = RegistryReceiveRemote("", "password1234")
        val deviceId = "device1"

        assertThrows(IllegalArgumentException::class.java) {
            userController.performRegistry(registryData, deviceId)
        }
    }

    @Test
    fun `testPerformRegistryWhenPasswordIsShortThenThrowsException`() {
        val registryData = RegistryReceiveRemote("email@example.com", "short")
        val deviceId = "device1"

        assertThrows(Exception::class.java) {
            userController.performRegistry(registryData, deviceId)
        }
    }

    @Test
    fun `testTryRevokeDeviceWhenUserAndSessionExistThenRevokeSession`() {
        val userId = 1
        val deviceId = "device1"
        val userDTO = UsersFetchDTO(userId, "email@example.com")
        val sessionDTO = SessionDTO(UUID.randomUUID(), userId, deviceId, "validToken1234".salt())

        every { authRepository.selectUserById(userId) } returns userDTO
        every { authRepository.getSessionByUserIdAndDeviceId(userId, deviceId) } returns sessionDTO

        userController.tryRevokeDevice(userId, deviceId)

        verify { authRepository.revokeSession(userId, deviceId) }
    }

    // tryConnectUser
    @Test
    fun `tryConnectUser returns valid session when token is correct`() {
        val userId = 1
        val deviceId = "device1"
        val token = "validToken1234"
        val userDTO = UsersFetchDTO(userId, "email@example.com")
        val sessionDTO = SessionDTO(UUID.randomUUID(), userId, deviceId, token.salt())

        every { authRepository.selectUserById(userId) } returns userDTO
        every { authRepository.getSessionByUserIdAndDeviceId(userId, deviceId) } returns sessionDTO
        every { authRepository.activateSession(any(), any(), any()) } returns sessionDTO

        val result = userController.tryConnectUser(userId, deviceId, token)

        assertNotNull(result)
        assertEquals(result.userId, userId.toString())
        assertEquals(result.sessionId, sessionDTO.sessionId.toString())
        assertEquals(result.userEmail, userDTO.email)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `tryConnectUser throws exception when token is incorrect`() {
        val userId = 1
        val deviceId = "device1"
        val token = "invalidToken1234"
        val userDTO = UsersFetchDTO(userId, "email@example.com")
        val sessionDTO = SessionDTO(UUID.randomUUID(), userId, deviceId, "differentSalt")

        every { authRepository.selectUserById(userId) } returns userDTO
        every { authRepository.getSessionByUserIdAndDeviceId(userId, deviceId) } returns sessionDTO

        userController.tryConnectUser(userId, deviceId, token)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `tryConnectUser throws exception when sessionDTO is null`() {
        val userId = 1
        val deviceId = "device1"
        val token = "invalidToken1234"
        val userDTO = UsersFetchDTO(userId, "email@example.com")

        every { authRepository.selectUserById(userId) } returns userDTO
        every { authRepository.getSessionByUserIdAndDeviceId(userId, deviceId) } returns null

        userController.tryConnectUser(userId, deviceId, token)
    }

    @Test
    fun `testGenerateTokenWhenGivenUserIdAndDeviceIdThenReturnCorrectToken`() {
        // Arrange
        val userId = 1
        val deviceId = "device1"
        val expectedToken = "$userId-$deviceId".salt().slice(0 until 16)

        // Act
        val actualToken = userController.generateToken(userId, deviceId)

        // Assert
        assertEquals(expectedToken, actualToken)
    }

    @Test
    fun `testGenerateTokenWhenGivenEmptyDeviceIdThenThrowException`() {
        // Arrange
        val userId = 1
        val deviceId = ""

        // Act & Assert
        val exception = assertThrows(IllegalArgumentException::class.java) {
            userController.generateToken(userId, deviceId)
        }
        assertEquals("DeviceId is empty.", exception.message)
    }

    @Test
    fun `testPerformLoginWhenUserExistsAndPasswordCorrectThenReturnSession`() {
        val enterData = EnterDataReceiveRemote("email@example.com", "password1234")
        val deviceId = "device1"
        val userDTO = UserAuthFetchDTO(1, enterData.email, enterData.password.salt())
        val sessionResponse = SessionResponseRemote("1", "1", "email@example.com")

        every { authRepository.fetchUserByEmail(enterData.email) } returns userDTO
        every { authRepository.createTemporarySession(any(), any(), any()) } returns sessionResponse

        val result = userController.performLogin(enterData, deviceId)

        TestCase.assertNotNull(result)
        TestCase.assertEquals(result.statusCode, HttpStatusCode.OK)
        TestCase.assertEquals(result.data, sessionResponse)
    }

    @Test
    fun `testPerformLoginWhenUserDoesNotExistThenReturnBadRequest`() {
        val enterData = EnterDataReceiveRemote("email@example.com", "password1234")
        val deviceId = "device1"

        every { authRepository.fetchUserByEmail(enterData.email) } returns null

        val result = userController.performLogin(enterData, deviceId)

        TestCase.assertNotNull(result)
        TestCase.assertEquals(result.statusCode, HttpStatusCode.BadRequest)
        TestCase.assertEquals(result.errorMessage, "User not found")
    }

    @Test
    fun testPerformLoginWhenPasswordIncorrectThenReturnBadRequest() {
        val enterData = EnterDataReceiveRemote("email@example.com", "password1234")
        val deviceId = "device1"
        val correctPasswordSalt = "correctPasswordSalt".salt()
        val userDTO = UserAuthFetchDTO(1, email = "email@example.com", password = correctPasswordSalt)

        every { authRepository.fetchUserByEmail(enterData.email) } returns userDTO

        val result = userController.performLogin(enterData, deviceId)

        TestCase.assertNotNull(result)
        TestCase.assertEquals(result.statusCode, HttpStatusCode.BadRequest)
        TestCase.assertEquals(result.errorMessage, "Invalid password")
    }
}
