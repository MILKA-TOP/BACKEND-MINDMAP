package mmap.features.catalog

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import mmap.domain.catalog.CatalogRepository
import mmap.domain.catalog.models.MapCatalogRemote
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CatalogControllerTest {

    private lateinit var catalogRepository: CatalogRepository
    private lateinit var catalogController: CatalogController

    @BeforeTest
    fun setUp() {
        catalogRepository = mockk(relaxed = true)
        catalogController = CatalogController(catalogRepository)
    }

    @Test
    fun `getAddedDiagrams returns user diagrams successfully`() {
        val userId = 1

        val tmp1 = mock<MapCatalogRemote>().copy(id = "Diagram 1", title = "Description 1")
        val tmp2 = mock<MapCatalogRemote>().copy(id = "Diagram 2", title = "Description 2")
        val expectedDiagrams = listOf(tmp1, tmp2)
        every { catalogRepository.getUserDiagrams(userId) } returns expectedDiagrams

        val diagrams = catalogController.getAddedDiagrams(userId)

        assertEquals(expectedDiagrams, diagrams)
    }

    @Test
    fun `searchMaps throws IllegalArgumentException for short query`() {
        val userId = 1
        val shortQuery = "ab"

        val exception = assertThrows(IllegalArgumentException::class.java) {
            catalogController.searchMaps(userId, shortQuery)
        }

        assertEquals("The length og query must be more, than 3", exception.message)
    }

    @Test
    fun `searchMaps returns diagrams matching query`() {
        val userId = 1
        val query = "Diagram"
        val tmp1 = mock<MapCatalogRemote>().copy(id = "Diagram 1", title = "Description 1")
        val tmp2 = mock<MapCatalogRemote>().copy(id = "Diagram 2", title = "Description 2")
        val expectedDiagrams = listOf(tmp1, tmp2)

        every { catalogRepository.getDiagramsByQuery(userId, query) } returns (expectedDiagrams)

        val diagrams = catalogController.searchMaps(userId, query)

        assertEquals(expectedDiagrams, diagrams)
    }

    @Test
    fun `testGetAddedDiagramsWhenValidUserIdThenReturnDiagrams`() {
        val userId = 1
        val diagrams = listOf<MapCatalogRemote>()

        every { catalogRepository.getUserDiagrams(userId) } returns diagrams

        val result = catalogController.getAddedDiagrams(userId)

        assertNotNull(result)
        verify { catalogRepository.getUserDiagrams(userId) }
    }

    @Test
    fun `testSearchMapsWhenQueryIsValidThenReturnResults`() {
        val userId = 1
        val query = "example map"
        val results = listOf<MapCatalogRemote>()

        every { catalogRepository.getDiagramsByQuery(userId, query) } returns results

        val result = catalogController.searchMaps(userId, query)

        assertNotNull(result)
        assertEquals(result, results)
        verify { catalogRepository.getDiagramsByQuery(userId, query) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `testSearchMapsWhenQueryIsShortThenThrowException`() {
        val userId = 1
        val query = "ex"

        catalogController.searchMaps(userId, query)
    }

    @Test
    fun `testSearchMapsWhenNoResultsThenReturnEmptyList`() {
        val userId = 1
        val query = "nonexistent"
        val results = emptyList<MapCatalogRemote>()

        every { catalogRepository.getDiagramsByQuery(userId, query) } returns results

        val result = catalogController.searchMaps(userId, query)

        assertTrue(result.isEmpty())
        verify { catalogRepository.getDiagramsByQuery(userId, query) }
    }

    @Test
    fun `testGetAddedDiagramsWhenUserIdInvalidThenReturnEmptyList`() {
        val userId = -1
        val results = emptyList<MapCatalogRemote>()

        every { catalogRepository.getUserDiagrams(userId) } returns results

        val result = catalogController.getAddedDiagrams(userId)

        assertTrue(result.isEmpty())
        verify { catalogRepository.getUserDiagrams(userId) }
    }
}
