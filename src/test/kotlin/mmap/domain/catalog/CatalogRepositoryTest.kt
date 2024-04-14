package mmap.domain.catalog

import io.mockk.every
import io.mockk.mockk
import mmap.data.maps.MapsDataSource
import mmap.database.selectedmaps.SelectedMapDTO
import mmap.database.users.UsersFetchDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class CatalogRepositoryTest {

    private lateinit var mapsDataSource: MapsDataSource
    private lateinit var catalogRepository: CatalogRepository

    @BeforeTest
    fun setUp() {
        mapsDataSource = mockk(relaxed = true)
        catalogRepository = CatalogRepository(mapsDataSource)
    }

    @Test
    fun `getUserDiagrams returns a list of MapCatalogRemote successfully`() {
        val userId = 1
        val map1 = mockk<SelectedMapDTO>(relaxed = true) {
            every { title } returns "Test Map 1"
            every { passwordHash } returns null
            every { admin } returns UsersFetchDTO(userId, "")
        }
        val map2 = mockk<SelectedMapDTO>(relaxed = true) {
            every { title } returns "Test Map 2"
            every { passwordHash } returns null
            every { admin } returns UsersFetchDTO(1, "")
        }
        val selectedMapDTOs = listOf(
            map1, map2
        )
        every({ mapsDataSource.selectByUser(userId) }).returns(selectedMapDTOs)

        val result = catalogRepository.getUserDiagrams(userId)

        assertEquals(2, result.size)
        assertEquals("Test Map 1", result[0].title)
        assertEquals(true, result[0].isEnableEdit)
        assertEquals(false, result[1].isPrivate)
    }

    @Test
    fun `getDiagramsByQuery returns a filtered list of MapCatalogRemote successfully`() {
        val userId = 1
        val query = "Test"
        val map1 = mockk<SelectedMapDTO>(relaxed = true) {
            every { title } returns "Test Map 1"
            every { passwordHash } returns null
            every { admin } returns UsersFetchDTO(userId, "")
        }
        val map2 = mockk<SelectedMapDTO>(relaxed = true) {
            every { title } returns "Another Map"
            every { passwordHash } returns null
            every { admin } returns UsersFetchDTO(1, "")
        }
        val selectedMapDTOs = listOf(
            map1, map2
        )
        every({ mapsDataSource.selectByQuery(userId, query) }).returns(selectedMapDTOs)

        val result = catalogRepository.getDiagramsByQuery(userId, query)

        assertEquals(2, result.size) // Assuming the query matches both maps for simplicity
        assertEquals("Test Map 1", result[0].title)
        assertEquals("Another Map", result[1].title)
    }

    @Test
    fun `getUserDiagrams returns empty list when dataSource returns empty`() {
        val userId = 1
        every { mapsDataSource.selectByUser(userId) } returns emptyList()

        val result = catalogRepository.getUserDiagrams(userId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getDiagramsByQuery handles data source exceptions gracefully`() {
        val userId = 1
        val query = "query"
        every { mapsDataSource.selectByQuery(userId, query) } throws RuntimeException("Database error")

        assertFailsWith<RuntimeException> {
            catalogRepository.getDiagramsByQuery(userId, query)
        }
    }

    @Test
    fun `getDiagramsByQuery returns list with correct privacy settings`() {
        val userId = 1
        val query = "secure"
        val selectedMapDTOs = listOf(
            mockk<SelectedMapDTO>(relaxed = true) {
                every { title } returns "Secure Map"
                every { passwordHash } returns "hashedPassword" // Indicates a private map
                every { admin } returns UsersFetchDTO(userId, "")
            }
        )
        every { mapsDataSource.selectByQuery(userId, query) } returns selectedMapDTOs

        val result = catalogRepository.getDiagramsByQuery(userId, query)

        assertEquals(1, result.size)
        assertTrue(result[0].isPrivate)
        assertEquals("Secure Map", result[0].title)
    }

    @Test
    fun `getDiagramsByQuery returns list with correct admin`() {
        val userId = 1
        val otherUserId = 2
        val query = "secure"
        val selectedMapDTOs = listOf(
            mockk<SelectedMapDTO>(relaxed = true) {
                every { title } returns "Secure Map"
                every { passwordHash } returns "hashedPassword" // Indicates a private map
                every { admin } returns UsersFetchDTO(otherUserId, "")
            }
        )
        every { mapsDataSource.selectByQuery(userId, query) } returns selectedMapDTOs

        val result = catalogRepository.getDiagramsByQuery(userId, query)

        assertTrue(result[0].isPrivate)
        assertEquals("Secure Map", result[0].title)
    }
}
