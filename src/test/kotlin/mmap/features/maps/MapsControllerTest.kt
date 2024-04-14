package mmap.features.maps

import io.ktor.http.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import mmap.database.maps.SelectMapDTO
import mmap.domain.maps.MapsRepository
import mmap.domain.maps.models.request.*
import mmap.domain.maps.models.response.SummaryEditMapResponseRemote
import mmap.extensions.AccessDenied
import mmap.extensions.salt
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.anyInt
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MapsControllerTest {

    private lateinit var mapsRepository: MapsRepository
    private lateinit var mapsController: MapsController

    private val userId = 1
    private val mapId = 1
    private val requestUserId = 1
    private val fetchUserId = 1 // For simplicity, using the same user ID for request and fetch

    @BeforeTest
    fun setup() {
        mapsRepository = mockk<MapsRepository>(relaxed = true)
        mapsController = MapsController(mapsRepository)
    }

    @Test
    fun testCreateNewMapWhenCalledThenReturnsCorrectMapIdResponseRemote() {
        // Arrange
        val userId = anyInt()
        val crateParams = mockk<MapsCreateRequestParams>()
        val expectedMapId = anyInt()
        every({ mapsRepository.createNewMap(userId, crateParams) }).returns(expectedMapId)

        // Act
        val actualMapIdResponseRemote = mapsController.createNewMap(userId, crateParams)

        // Assert
        verify(exactly = 1) {
            mapsRepository.createNewMap(userId, crateParams)
        }
        assertEquals(expectedMapId, actualMapIdResponseRemote.mapId.toInt())
    }

    @Test
    fun `migrate returns MapIdResponseRemote successfully`() {
        val migrateParams = MapsMigrateRequestParams(
            text = "Here is some text",
            type = MigrateType.MINDOMO_TEXT,
            password = null,
        )
        every({ mapsRepository.migrate(userId, migrateParams) }).returns(mapId)

        val response = mapsController.migrate(userId, migrateParams)

        assertEquals(mapId.toString(), response.mapId)
    }

    @Test
    fun `migrate handles repository exception gracefully`() {
        val migrateParams = MapsMigrateRequestParams(
            text = "Here is some text",
            type = MigrateType.MINDOMO_TEXT,
            password = null,
        )
        every({ mapsRepository.migrate(userId, migrateParams) }).throws(RuntimeException("Migration failed"))

        val exception = assertThrows<RuntimeException> {
            mapsController.migrate(userId, migrateParams)
        }

        assertEquals("Migration failed", exception.message)
    }

    // addNewMap

    @Test
    fun `addNewMap adds map successfully without password`() {
        val addParams = MapsAddRequestParams(mapId = mapId.toString(), password = null)
        val mapPreview = mockk<SelectMapDTO>(relaxed = false) {
            every { passwordHash } returns null
        }

        every { mapsRepository.selectMapPreview(mapId) }.returns(mapPreview)

        val response = mapsController.addNewMap(userId, addParams)

        assertEquals(HttpStatusCode.OK, response.statusCode)
    }

    @Test
    fun `addNewMap returns BadRequest when incorrect password provided for protected map`() {
        val addParams = MapsAddRequestParams(mapId = mapId.toString(), password = "wrongPassword")
        val mapPreview = mockk<SelectMapDTO>(relaxed = true).copy(passwordHash = "hashedPassword".salt())

        every({ mapsRepository.selectMapPreview(mapId) }).returns(mapPreview)

        val response = mapsController.addNewMap(userId, addParams)

        assertEquals(HttpStatusCode.BadRequest, response.statusCode)
        assertEquals("Incorrect password parameter", response.errorMessage)
    }

    @Test
    fun `addNewMap adds map successfully with correct password`() {
        val correctPassword = "correctPassword"
        val hashedPassword =
            correctPassword.salt() // Assuming a salt extension function exists and is used in MapsController
        val addParams = MapsAddRequestParams(mapId = mapId.toString(), password = correctPassword)
        val mapPreview = mockk<SelectMapDTO>(relaxed = true) {
            every { passwordHash } returns hashedPassword
        }

        every({ mapsRepository.selectMapPreview(mapId) }).returns(mapPreview)

        val response = mapsController.addNewMap(userId, addParams)

        assertEquals(HttpStatusCode.OK, response.statusCode)
    }

    @Test
    fun `addNewMap returns BadRequest when password required but not provided`() {
        val addParams = MapsAddRequestParams(mapId = mapId.toString(), password = null)
        val mapPreview = mockk<SelectMapDTO>(relaxed = true).copy(passwordHash = "hashedPassword".salt())

        every({ mapsRepository.selectMapPreview(mapId) }).returns(mapPreview)

        val response = mapsController.addNewMap(userId, addParams)

        assertEquals(HttpStatusCode.BadRequest, response.statusCode)
        assertEquals("Incorrect password parameter", response.errorMessage)
    }

    @Test
    fun `addNewMap returns BadRequest when password not required but provided`() {
        val addParams = MapsAddRequestParams(mapId = mapId.toString(), password = "notNullPassword")
        val mapPreview = mockk<SelectMapDTO>(relaxed = true) {
            every { passwordHash } returns null
        }

        every({ mapsRepository.selectMapPreview(mapId) }).returns(mapPreview)

        val response = mapsController.addNewMap(userId, addParams)

        assertEquals(HttpStatusCode.BadRequest, response.statusCode)
        assertEquals("Incorrect password parameter", response.errorMessage)
    }

    // fetch

    @Test
    fun `fetch returns SummaryMapResponseRemote successfully`() {
        val summaryMapResponse = mockk<SummaryEditMapResponseRemote>(relaxed = false) {
            every { id } returns mapId.toString()
            every { title } returns "Test Map"
            every { description } returns "A test map"
        }
        every({ mapsRepository.fetch(requestUserId, mapId, fetchUserId) }).returns(summaryMapResponse)

        val response = mapsController.fetch(requestUserId, mapId, fetchUserId)

        assertEquals(HttpStatusCode.OK, response.statusCode)
        assertEquals(summaryMapResponse, response.data)
    }

    @Test
    fun `fetch returns AccessDenied when user does not have access to the map`() {
        every({ mapsRepository.fetch(requestUserId, mapId, fetchUserId) }).returns(null)

        val response = mapsController.fetch(requestUserId, mapId, fetchUserId)

        assertEquals(AccessDenied, response.statusCode)
        assertEquals("You doesn't contains this map in your catalog", response.errorMessage)
    }

    @Test
    fun `fetch handles non-existent maps gracefully`() {
        every({ mapsRepository.fetch(requestUserId, mapId, fetchUserId) }).returns(null)

        val response = mapsController.fetch(requestUserId, mapId, fetchUserId)

        assertEquals(AccessDenied, response.statusCode)
        assertEquals("You doesn't contains this map in your catalog", response.errorMessage)
    }

    @Test
    fun `fetch success without fetchUserId`() {
        val summaryMapResponse = mockk<SummaryEditMapResponseRemote>(relaxed = false) {
            every { id } returns mapId.toString()
            every { title } returns "Test Map"
            every { description } returns "A test map"
        }
        every({ mapsRepository.fetch(requestUserId, mapId, requestUserId) }).returns(summaryMapResponse)

        val response = mapsController.fetch(requestUserId, mapId)

        assertEquals(HttpStatusCode.OK, response.statusCode)
        assertEquals(summaryMapResponse, response.data)
    }

    // erase

    @Test
    fun `eraseInteractedMaps succeeds when user has access`() {
        every({ mapsRepository.isEnabledInteractForUserByMapId(mapId, userId) }).returns(true)

        val response = mapsController.eraseInteractedMaps(mapId, userId, MapRemoveType.HIDE)

        assertEquals(HttpStatusCode.OK, response.statusCode)
    }

    @Test
    fun `eraseInteractedMaps returns Conflict when user does not have access`() {
        every({ mapsRepository.isEnabledInteractForUserByMapId(mapId, userId) }).returns(false)

        val response = mapsController.eraseInteractedMaps(mapId, userId, MapRemoveType.HIDE)

        assertEquals(HttpStatusCode.Conflict, response.statusCode)
        assertEquals("You doesn't have access for this node and map", response.errorMessage)
    }

    // delete

    @Test
    fun `deleteEditableMap succeeds when user is the map admin`() {
        every({ mapsRepository.selectAdminId(mapId) }).returns(userId)

        val response = mapsController.deleteEditableMap(mapId, userId)

        assertEquals(HttpStatusCode.OK, response.statusCode)
    }

    @Test
    fun `deleteEditableMap returns Conflict when user is not the map admin`() {
        every({ mapsRepository.selectAdminId(mapId) }).returns(userId + 1) // Different user ID

        val response = mapsController.deleteEditableMap(mapId, userId)

        assertEquals(HttpStatusCode.Conflict, response.statusCode)
        assertEquals("You doesn't have access for this node and map", response.errorMessage)
    }
}
