package mmap.features.testing

import io.ktor.http.*
import mmap.core.ApiResponse
import mmap.domain.tests.TestsRepository
import mmap.domain.tests.models.TestingCompleteRequestRemote
import mmap.domain.maps.models.response.TestResultViewResponseRemote
import mmap.domain.maps.models.response.TestsEditResponseRemote
import java.util.*

class TestingController(private val testingRepository: TestsRepository) {

    fun completeTest(
        userId: Int,
        testId: UUID,
        usersTestAnswers: TestingCompleteRequestRemote
    ): ApiResponse<TestResultViewResponseRemote> {
        val isNodeEnabled = testingRepository.isEnabledInteractForUserByTestId(testId, userId)

        if (!isNodeEnabled) return ApiResponse(
            HttpStatusCode.Conflict,
            errorMessage = "You doesn't have access for this node and map"
        )

        val lastTestsFetchId = testingRepository.selectFetchIdForTest(testId, userId)
        if (lastTestsFetchId != null) return ApiResponse(
            HttpStatusCode.Conflict,
            errorMessage = "You can't complete this test again"
        )

        val completeTestResult = testingRepository.completeTest(userId, testId, usersTestAnswers)
        return ApiResponse(data = completeTestResult)
    }

    suspend fun generateTest(userId: Int, nodeId: UUID): TestsEditResponseRemote {
        return testingRepository.generateTest(userId, nodeId)
    }
}
