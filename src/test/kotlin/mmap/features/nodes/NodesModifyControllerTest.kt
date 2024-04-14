package mmap.features.nodes

import io.ktor.http.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import mmap.domain.nodes.NodesRepository
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class NodesModifyControllerTest {

    private lateinit var nodesRepository: NodesRepository
    private lateinit var nodesModifyController: NodesModifyController
    private val userId = 1
    private val nodeId = UUID.randomUUID()

    @BeforeTest
    fun setUp() {
        nodesRepository = mockk(relaxed = true)
        nodesModifyController = NodesModifyController(nodesRepository)
    }

    @Test
    fun `toggleSelection returns conflict when node is not enabled for user`() {
        every { nodesRepository.isEnabledInteractForUserByNodeId(nodeId, userId) } returns false

        val response = nodesModifyController.toggleSelection(userId, nodeId)

        assertEquals(HttpStatusCode.Conflict, response.statusCode)
        assertEquals("You doesn't have access for this node and map", response.errorMessage)
    }

    @Test
    fun `toggleSelection marks node as enabled when previously disabled`() {
        every { nodesRepository.isEnabledInteractForUserByNodeId(nodeId, userId) }.returns(true)
        every { nodesRepository.toggleNode(nodeId, userId) }.returns(true)

        val response = nodesModifyController.toggleSelection(userId, nodeId)

        assertEquals(HttpStatusCode.OK, response.statusCode)
        assertEquals(true, response.data?.isMarked)
    }

    @Test
    fun `toggleSelection marks node as disabled when previously enabled`() {
        every { nodesRepository.isEnabledInteractForUserByNodeId(nodeId, userId) }.returns(true)
        every { nodesRepository.toggleNode(nodeId, userId) }.returns(false)

        val response = nodesModifyController.toggleSelection(userId, nodeId)

        assertEquals(HttpStatusCode.OK, response.statusCode)
        assertEquals(false, response.data?.isMarked)
    }

    @Test(RuntimeException::class)
    fun `toggleSelection handles repository exceptions gracefully`() {
        every {
            nodesRepository.isEnabledInteractForUserByNodeId(
                nodeId,
                userId
            )
        } throws RuntimeException("Database error")

        nodesModifyController.toggleSelection(userId, nodeId)
    }

    @Test
    fun `toggleSelection verifies node existence before toggling`() {
        every { nodesRepository.isEnabledInteractForUserByNodeId(nodeId, userId) } returns true
        every { nodesRepository.toggleNode(nodeId, userId) } returns false

        val response = nodesModifyController.toggleSelection(userId, nodeId)

        verify(exactly = 1) {
            nodesRepository.isEnabledInteractForUserByNodeId(nodeId, userId)
            nodesRepository.toggleNode(nodeId, userId)
        }
        assertEquals(HttpStatusCode.OK, response.statusCode)
    }
}