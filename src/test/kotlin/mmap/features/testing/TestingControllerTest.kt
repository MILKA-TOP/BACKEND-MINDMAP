package mmap.features.testing

import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import mmap.domain.maps.models.response.TestResultViewResponseRemote
import mmap.domain.maps.models.response.TestsEditResponseRemote
import mmap.domain.tests.TestsRepository
import mmap.domain.tests.models.TestingCompleteRequestRemote
import org.junit.Assert.assertThrows
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TestingControllerTest {

    private lateinit var testingRepository: TestsRepository
    private lateinit var testingController: TestingController

    private val userId = 1
    private val testId = UUID.randomUUID()
    private val nodeId = UUID.randomUUID()

    private val completeRequestRemote = mockk<TestingCompleteRequestRemote>()

    @BeforeTest
    fun setUp() {
        testingRepository = mockk(relaxed = true)
        testingController = TestingController(testingRepository)
    }

    @Test
    fun `completeTest returns conflict when node is not enabled`() {
        every({ testingRepository.isEnabledInteractForUserByTestId(testId, userId) }).returns(false)

        val response = testingController.completeTest(userId, testId, completeRequestRemote)

        assertEquals(HttpStatusCode.Conflict, response.statusCode)
        assertEquals("You doesn't have access for this node and map", response.errorMessage)
    }

    @Test
    fun `completeTest returns conflict when test is already completed`() {
        every({ testingRepository.isEnabledInteractForUserByTestId(testId, userId) }).returns(true)
        every({ testingRepository.selectFetchIdForTest(testId, userId) }).returns(UUID.randomUUID())

        val response = testingController.completeTest(userId, testId, completeRequestRemote)

        assertEquals(HttpStatusCode.Conflict, response.statusCode)
        assertEquals("You can't complete this test again", response.errorMessage)
    }

    @Test
    fun `completeTest completes successfully`() {
        val resultView = mockk<TestResultViewResponseRemote>()

        every({ testingRepository.isEnabledInteractForUserByTestId(testId, userId) }).returns(true)
        every({ testingRepository.selectFetchIdForTest(testId, userId) }).returns(null)
        every({ testingRepository.completeTest(userId, testId, completeRequestRemote) }).returns(resultView)

        val response = testingController.completeTest(userId, testId, completeRequestRemote)

        assertEquals(HttpStatusCode.OK, response.statusCode)
        assertEquals(TestResultViewResponseRemote::class.java, response.data?.javaClass)
    }

    @Test
    fun `generateTest generates a test successfully`() = testApplication {
        val editResponse = mockk<TestsEditResponseRemote>()
        coEvery({ testingRepository.generateTest(userId, nodeId) }).returns(editResponse)

        val response = testingController.generateTest(userId, nodeId)

        assertEquals(TestsEditResponseRemote::class.java, response.javaClass)
    }

    @Test
    fun `completeTest handles exception when repository method fails`() {
        every { testingRepository.isEnabledInteractForUserByTestId(testId, userId) } returns true
        every { testingRepository.selectFetchIdForTest(testId, userId) } returns null
        every { testingRepository.completeTest(userId, testId, completeRequestRemote) } throws RuntimeException("Unexpected error")

        assertThrows(RuntimeException::class.java) {
            testingController.completeTest(userId, testId, completeRequestRemote)
        }
    }
}
