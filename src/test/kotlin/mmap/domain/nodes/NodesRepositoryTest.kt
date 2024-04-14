package mmap.domain.nodes

import io.mockk.every
import io.mockk.mockk
import mmap.data.maps.MapsDataSource
import mmap.data.maps.NodesDataSource
import org.junit.jupiter.api.Assertions.*
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class NodesRepositoryTest {

    private lateinit var mapsDataSource: MapsDataSource
    private lateinit var nodesDataSource: NodesDataSource
    private lateinit var nodesRepository: NodesRepository

    @BeforeTest
    fun setUp() {
        mapsDataSource = mockk(relaxed = false)
        nodesDataSource = mockk(relaxed = false)
        nodesRepository = NodesRepository(mapsDataSource, nodesDataSource)
    }

    @Test
    fun `isEnabledInteractForUserByNodeId returns true when interaction is enabled`() {
        val nodeId = UUID.randomUUID()
        val userId = 1
        every({ mapsDataSource.isEnabledInteractForUserByNodeId(nodeId, userId) }).returns(true)

        val result = nodesRepository.isEnabledInteractForUserByNodeId(nodeId, userId)

        assertTrue(result)
    }

    @Test
    fun `toggleNode toggles the node state successfully`() {
        val nodeId = UUID.randomUUID()
        val userId = 1
        every({ nodesDataSource.toggleNode(nodeId, userId) }).returns(true)

        val result = nodesRepository.toggleNode(nodeId, userId)

        assertTrue(result)
    }

    @Test
    fun `isEnabledInteractForUserByNodeId returns false when interaction is not enabled`() {
        val nodeId = UUID.randomUUID()
        val userId = 1
        every { mapsDataSource.isEnabledInteractForUserByNodeId(nodeId, userId) } returns false

        val result = nodesRepository.isEnabledInteractForUserByNodeId(nodeId, userId)

        assertFalse(result)
    }

    @Test
    fun `toggleNode returns false when toggling fails`() {
        val nodeId = UUID.randomUUID()
        val userId = 1
        every { nodesDataSource.toggleNode(nodeId, userId) } returns false

        val result = nodesRepository.toggleNode(nodeId, userId)

        assertFalse(result)
    }

    @Test
    fun `toggleNode handles exceptions gracefully`() {
        val nodeId = UUID.randomUUID()
        val userId = 1
        every { nodesDataSource.toggleNode(nodeId, userId) } throws RuntimeException("Database failure")

        val exception = assertFailsWith<RuntimeException> {
            nodesRepository.toggleNode(nodeId, userId)
        }

        assertEquals("Database failure", exception.message)
    }
}