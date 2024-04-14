package mmap.features.maps

import io.ktor.http.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import mmap.domain.maps.MapsEditRepository
import mmap.domain.maps.models.request.MapsUpdateRequestParams
import mmap.extensions.AccessDenied
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MapsEditUpdateControllerTest {

    private lateinit var mapsEditRepository: MapsEditRepository
    private lateinit var mapsEditUpdateController: MapsEditUpdateController

    private val userId = 1
    private val mapId = 1
    private val updatedParams = mockk<MapsUpdateRequestParams>(relaxed = true)

    @BeforeTest
    fun setup() {
        mapsEditRepository = mockk(relaxed = true)
        mapsEditUpdateController = MapsEditUpdateController(mapsEditRepository)
    }

    @Test
    fun `testUpdateWhenUserIsAdminThenReturnSuccessResponse`() = runBlocking {
        every { mapsEditRepository.selectAdminId(mapId) } returns userId

        val response = mapsEditUpdateController.update(userId, mapId, updatedParams)

        assertEquals(HttpStatusCode.OK, response.statusCode)
    }

    @Test
    fun `testUpdateWhenUserIsNotAdminThenReturnAccessDeniedResponse`() = runBlocking {
        every { mapsEditRepository.selectAdminId(mapId) } returns userId + 1 // Different user

        val response = mapsEditUpdateController.update(userId, mapId, updatedParams)

        assertEquals(AccessDenied, response.statusCode)
        assertEquals("You can't update this map", response.errorMessage)
    }

    @Test
    fun `update returns success when user is map admin`() = runBlocking {
        every({ mapsEditRepository.selectAdminId(mapId) }).returns(userId)

        val response = mapsEditUpdateController.update(userId, mapId, updatedParams)

        assertEquals(HttpStatusCode.OK, response.statusCode)
    }

    @Test
    fun `update returns AccessDenied when user is not map admin`() = runBlocking {
        every({ mapsEditRepository.selectAdminId(mapId) }).returns(userId + 1) // Different user

        val response = mapsEditUpdateController.update(userId, mapId, updatedParams)

        assertEquals(AccessDenied, response.statusCode)
        assertEquals("You can't update this map", response.errorMessage)
    }
}
