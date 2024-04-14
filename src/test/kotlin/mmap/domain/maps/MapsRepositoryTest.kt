package mmap.domain.maps

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import mmap.data.maps.MapsDataSource
import mmap.data.migrate.MigrateDataSource
import mmap.database.answers.AnswersDTO
import mmap.database.maps.SelectMapDTO
import mmap.database.maps.SummaryEditSelectMapDTO
import mmap.database.maps.SummaryViewSelectMapDTO
import mmap.database.nodes.NodesDTO
import mmap.database.questions.QuestionsDTO
import mmap.database.selectedmaps.SelectedMapDTO
import mmap.database.tests.TestsDTO
import mmap.database.users.UsersFetchDTO
import mmap.domain.maps.models.request.MapRemoveType
import mmap.domain.maps.models.request.MapsCreateRequestParams
import mmap.domain.maps.models.request.MapsMigrateRequestParams
import mmap.domain.maps.models.request.MigrateType
import mmap.domain.maps.models.response.SummaryEditMapResponseRemote
import mmap.domain.maps.models.response.SummaryViewMapResponseRemote
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class MapsRepositoryTest {

    private lateinit var mapsDataSource: MapsDataSource
    private lateinit var migrateDataSource: MigrateDataSource
    private lateinit var mapsRepository: MapsRepository

    @BeforeTest
    fun setUp() {
        mapsDataSource = mockk(relaxed = true)
        migrateDataSource = mockk(relaxed = true)
        mapsRepository = MapsRepository(mapsDataSource, migrateDataSource)
    }

    @Test
    fun `createNewMap creates a new map successfully`() {
        val userId = 1
        val createParams = MapsCreateRequestParams(
            title = "New Map", description = "A description for the new map", password = "password123"
        )
        val expectedMapId = 42
        every { mapsDataSource.insertMap(any()) }.returns(expectedMapId)

        val mapId = mapsRepository.createNewMap(userId, createParams)

        assertEquals(expectedMapId, mapId)
        verify {
            (mapsDataSource).insertMap(any())
        }
    }

    @Test
    fun `createNewMap creates a new map successfully by ref and error isEnabledInteractForUserByMapId`() {
        val userId = 1
        val createParams = MapsCreateRequestParams(
            title = "New Map",
            description = "A description for the new map",
            password = "password123",
            ref = "someRefId"
        )
        val expectedMapId = 42
        every { mapsDataSource.insertMap(any()) }.returns(expectedMapId)
        every { mapsDataSource.isEnabledInteractForUserByMapId(any(), any()) }.returns(false)

        assertThrows<IllegalArgumentException>("Enabled copy map only for user, who save this map to catalog") {
            mapsRepository.createNewMap(userId, createParams)
        }
    }

    @Test
    fun `createNewMap creates a new map successfully by ref and error incorrectRef`() {
        val userId = 1
        val createParams = MapsCreateRequestParams(
            title = "New Map", description = "A description for the new map", password = null, ref = "incorrectRef"
        )
        val expectedMapId = 42
        every { mapsDataSource.insertMap(any()) }.returns(expectedMapId)
        every { mapsDataSource.isEnabledInteractForUserByMapId(any(), any()) }.returns(true)

        assertThrows<NumberFormatException>() {
            mapsRepository.createNewMap(userId, createParams)
        }
    }

    @Test
    fun `createNewMap creates a new map successfully by ref and success`() {
        val userId = 1
        val returnMapId = 2
        val createParams = MapsCreateRequestParams(
            title = "New Map", description = "A description for the new map", password = "password123", ref = "2"
        )
        val nodeIdSave = UUID.randomUUID()
        val answerIdSave = UUID.randomUUID()
        val questionIdSave = UUID.randomUUID()
        val testIdSave = UUID.randomUUID()
        val editMap = mockk<SummaryEditSelectMapDTO>(relaxed = true) {
            every { nodes } returns listOf(mockk<NodesDTO>(relaxed = true) {
                every { id } returns nodeIdSave
            })
            every { answers } returns listOf(mockk<AnswersDTO>(relaxed = true) {
                every { id } returns answerIdSave
                every { questionId } returns questionIdSave
            })
            every { questions } returns listOf(mockk<QuestionsDTO>(relaxed = true) {
                every { id } returns questionIdSave
                every { testId } returns testIdSave
            })
            every { tests } returns listOf(mockk<TestsDTO>(relaxed = true) {
                every { id } returns testIdSave
                every { nodeId } returns nodeIdSave
            })
        }
        val expectedMapId = 42
        every { mapsDataSource.insertMap(any()) }.returns(expectedMapId)
        every { mapsDataSource.isEnabledInteractForUserByMapId(any(), any()) }.returns(true)
        every { mapsDataSource.fetchEditSummary(any()) }.returns(editMap)

        every { mapsDataSource.create(any()) } returns returnMapId

        mapsRepository.createNewMap(userId, createParams)
    }

    @Test
    fun `createNewMap creates a new map error password`() {
        val userId = 1
        val createParams = MapsCreateRequestParams(
            title = "New Map", description = "A description for the new map", password = "small"
        )
        val expectedMapId = 42
        every { mapsDataSource.insertMap(any()) }.returns(expectedMapId)

        assertThrows<IllegalArgumentException>("Incorrect password parameter") {
            mapsRepository.createNewMap(userId, createParams)
        }
    }

    @Test
    fun `createNewMap creates a new map error password small`() {
        val userId = 1
        val createParams = MapsCreateRequestParams(
            title = "New Map", description = "A description for the new map", password = "small"
        )
        val expectedMapId = 42
        every { mapsDataSource.insertMap(any()) }.returns(expectedMapId)

        assertThrows<IllegalArgumentException>("Incorrect password parameter") {
            mapsRepository.createNewMap(userId, createParams)
        }
    }

    @Test
    fun `createNewMap creates a new map error password trim`() {
        val userId = 1
        val createParams = MapsCreateRequestParams(
            title = "New Map", description = "A description for the new map", password = "small                  "
        )
        val expectedMapId = 42
        every { mapsDataSource.insertMap(any()) }.returns(expectedMapId)

        assertThrows<IllegalArgumentException>("Incorrect password parameter") {
            mapsRepository.createNewMap(userId, createParams)
        }
    }

    @Test
    fun `createNewMap creates a new map error title spaces`() {
        val userId = 1
        val createParams = MapsCreateRequestParams(
            title = "       ", description = "A description for the new map", password = "small"
        )
        val expectedMapId = 42
        every { mapsDataSource.insertMap(any()) }.returns(expectedMapId)

        assertThrows<IllegalArgumentException>("Incorrect title parameter") {
            mapsRepository.createNewMap(userId, createParams)
        }
    }

    @Test
    fun `createNewMap creates a new map error title empty`() {
        val userId = 1
        val createParams = MapsCreateRequestParams(
            title = "", description = "", password = ""
        )
        val expectedMapId = 42
        every { mapsDataSource.insertMap(any()) }.returns(expectedMapId)

        assertThrows<IllegalArgumentException>("Incorrect title parameter") {
            mapsRepository.createNewMap(userId, createParams)
        }
    }

    @Test
    fun `migrate creates a new map from migration successfully`() {
        val userId = 1
        val migrateParams = MapsMigrateRequestParams(
            text = "Map data to migrate", type = MigrateType.MINDOMO_TEXT, password = "password123"
        )
        val expectedMapId = 42
        val create = mockk<SummaryEditSelectMapDTO>(relaxed = true)
        every({ migrateDataSource.migrateOtherMindMap(any(), any()) }).returns(create)
        every({ mapsDataSource.create(any(), any(), any(), any(), any()) }).returns(expectedMapId)

        val mapId = mapsRepository.migrate(userId, migrateParams)

        assertEquals(expectedMapId, mapId)
        verify { (migrateDataSource).migrateOtherMindMap(any(), any()) }
        verify { (mapsDataSource).create(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `migrate creates a new map without pass from migration successfully`() {
        val userId = 1
        val migrateParams = MapsMigrateRequestParams(
            text = "Map data to migrate", type = MigrateType.MINDOMO_TEXT, password = null
        )
        val expectedMapId = 42
        val create = mockk<SummaryEditSelectMapDTO>(relaxed = true)
        every({ migrateDataSource.migrateOtherMindMap(any(), any()) }).returns(create)
        every({ mapsDataSource.create(any(), any(), any(), any(), any()) }).returns(expectedMapId)

        val mapId = mapsRepository.migrate(userId, migrateParams)

        assertEquals(expectedMapId, mapId)
    }

    @Test
    fun `migrate password error`() {
        val userId = 1
        val migrateParams = MapsMigrateRequestParams(
            text = "Map data to migrate", type = MigrateType.MINDOMO_TEXT, password = "pass         "
        )

        assertThrows<IllegalArgumentException>() {
            mapsRepository.migrate(userId, migrateParams)
        }
    }

    @Test
    fun `selectMapPreview selects a map preview successfully`() {
        val mapId = 42
        val expectedSelectMapDTO = mockk<SelectMapDTO>(relaxed = true)
        every({ mapsDataSource.selectPreview(mapId) }).returns(expectedSelectMapDTO)

        val selectMapDTO = mapsRepository.selectMapPreview(mapId)

        assertEquals(expectedSelectMapDTO, selectMapDTO)
        verify { mapsDataSource.selectPreview(mapId) }
    }

    @Test
    fun `fetch returns a map summary no map null successfully`() {
        val requestUserId = 1
        val mapId = 42
        val fetchUserId = 1
        every({ mapsDataSource.selectByUser(requestUserId) }).returns(emptyList())

        val summaryMapResponseRemote = mapsRepository.fetch(requestUserId, mapId, fetchUserId)

        assertEquals(null, summaryMapResponseRemote)
        verify { (mapsDataSource).selectByUser(requestUserId) }
    }

    @Test
    fun `fetch edit returns a map summary successfully`() {
        val requestUserId = 1
        val mapId = 42
        val fetchUserId = 1
        val selectedMapDTO = mockk<SelectedMapDTO>(relaxed = true) {
            every { id } returns mapId
            every { admin.id } returns requestUserId
        }
        val nodeIdSave = UUID.randomUUID()
        val answerIdSave = UUID.randomUUID()
        val questionIdSave = UUID.randomUUID()
        val testIdSave = UUID.randomUUID()
        val expectedSummaryMapResponseRemote = mockk<SummaryEditSelectMapDTO>(relaxed = true) {
            every { admin.id } returns requestUserId
            every { id } returns mapId
            every { nodes } returns listOf(mockk<NodesDTO>(relaxed = true) {
                every { id } returns nodeIdSave
            })
            every { answers } returns listOf(mockk<AnswersDTO>(relaxed = true) {
                every { id } returns answerIdSave
                every { questionId } returns questionIdSave
            })
            every { questions } returns listOf(mockk<QuestionsDTO>(relaxed = true) {
                every { id } returns questionIdSave
                every { testId } returns testIdSave
            })
            every { accessUsers } returns listOf(
                UsersFetchDTO(2, ""),
                UsersFetchDTO(requestUserId, ""),
            )
            every { tests } returns listOf(mockk<TestsDTO>(relaxed = true) {
                every { id } returns testIdSave
                every { nodeId } returns nodeIdSave
            })
        }
        every({ mapsDataSource.selectByUser(requestUserId) }).returns(listOf(selectedMapDTO))
        every({ mapsDataSource.fetchEditSummary(mapId) }).returns(expectedSummaryMapResponseRemote)

        val summaryMapResponseRemote = mapsRepository.fetch(requestUserId, mapId, fetchUserId)

        assertNotNull(summaryMapResponseRemote)
        assertEquals(SummaryEditMapResponseRemote::class.java, summaryMapResponseRemote.javaClass)
        verify { (mapsDataSource).selectByUser(requestUserId) }
        verify { (mapsDataSource).fetchEditSummary(mapId) }
    }

    @Test
    fun `fetch view returns a map summary successfully`() {
        val requestUserId = 1
        val adminId = 99
        val mapId = 42
        val fetchUserId = 1
        val selectedMapDTO = mockk<SelectedMapDTO>(relaxed = true) {
            every { id } returns mapId
            every { admin.id } returns adminId
        }
        val nodeIdSave = UUID.randomUUID()
        val answerIdSave = UUID.randomUUID()
        val questionIdSave = UUID.randomUUID()
        val testIdSave = UUID.randomUUID()
        val expectedSummaryMapResponseRemote = mockk<SummaryViewSelectMapDTO>(relaxed = true) {
            every { admin.id } returns adminId
            every { id } returns mapId
            every { nodes } returns listOf(mockk<NodesDTO>(relaxed = true) {
                every { id } returns nodeIdSave
            })
            every { answers } returns listOf(mockk<AnswersDTO>(relaxed = true) {
                every { id } returns answerIdSave
                every { questionId } returns questionIdSave
            })
            every { questions } returns listOf(mockk<QuestionsDTO>(relaxed = true) {
                every { id } returns questionIdSave
                every { testId } returns testIdSave
            })
            every { tests } returns listOf(mockk<TestsDTO>(relaxed = true) {
                every { id } returns testIdSave
                every { nodeId } returns nodeIdSave
            })
        }
        every({ mapsDataSource.selectByUser(requestUserId) }).returns(listOf(selectedMapDTO))
        every({
            mapsDataSource.fetchViewSummary(
                mapId, requestUserId, any()
            )
        }).returns(expectedSummaryMapResponseRemote)

        val summaryMapResponseRemote = mapsRepository.fetch(requestUserId, mapId, fetchUserId)

        assertNotNull(summaryMapResponseRemote)
        assertEquals(SummaryViewMapResponseRemote::class.java, summaryMapResponseRemote.javaClass)
        verify { (mapsDataSource).selectByUser(requestUserId) }
        verify { (mapsDataSource).fetchViewSummary(mapId, requestUserId, any()) }
    }

    @Test
    fun `deleteEditableMap deletes a map successfully`() {
        val mapId = 42

        mapsRepository.deleteEditableMap(mapId)

        verify { (mapsDataSource).deleteEditableMap(mapId) }
    }

    @Test
    fun `isEnabledInteractForUserByMapId successfully`() {
        val mapId = 42
        val userId = 1

        mapsRepository.isEnabledInteractForUserByMapId(mapId, userId)

        verify { (mapsDataSource).isEnabledInteractForUserByMapId(mapId, userId) }
    }

    @Test
    fun `selectAdminId successfully`() {
        val mapId = 42

        mapsRepository.selectAdminId(mapId)

        verify { (mapsDataSource).selectAdminId(mapId) }
    }

    @Test
    fun `insertSelectionNewMap successfully`() {
        val mapId = 42
        val userId = 1

        mapsRepository.insertSelectionNewMap(mapId, userId)

        verify { (mapsDataSource).insertSelectionNewMap(mapId, userId) }
    }

    @Test
    fun `eraseInteractedMaps HIDE success`() {
        val mapId = 42
        val userId = 1

        mapsRepository.eraseInteractedMaps(mapId, userId, MapRemoveType.HIDE)

        verify { (mapsDataSource).hideMap(userId, mapId) }
    }

    @Test
    fun `eraseInteractedMaps DELETE success`() {
        val mapId = 42
        val userId = 1

        mapsRepository.eraseInteractedMaps(mapId, userId, MapRemoveType.DELETE)

        verify { (mapsDataSource).deleteInteractedData(userId, mapId) }
    }
}
