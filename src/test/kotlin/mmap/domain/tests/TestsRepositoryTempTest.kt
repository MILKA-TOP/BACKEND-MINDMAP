package mmap.domain.tests

import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import mmap.data.maps.MapsDataSource
import mmap.data.maps.NodesDataSource
import mmap.data.tests.opexams.OpexamsDataSource
import mmap.data.tests.yandex.Yandex300DataSource
import mmap.database.answers.AnswersDTO
import mmap.database.maps.SummaryEditSelectMapDTO
import mmap.database.nodes.NodesDTO
import mmap.database.questions.QuestionType
import mmap.database.questions.QuestionsDTO
import mmap.database.tests.StampedTestDTO
import mmap.domain.tests.models.AnswerCompleteRequestRemote
import mmap.domain.tests.models.QuestionCompleteRequestRemote
import mmap.domain.tests.models.TestingCompleteRequestRemote
import mmap.domain.tests.models.opexams.OpexamsQuestion
import org.junit.Assert
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestsRepositoryTempTest {

    private lateinit var mapsDataSource: MapsDataSource
    private lateinit var yandex300DataSource: Yandex300DataSource
    private lateinit var opexamsDataSource: OpexamsDataSource
    private lateinit var nodesDataSource: NodesDataSource
    private lateinit var testsRepository: TestsRepository
    private val nodeId = UUID.randomUUID()
    private val mapId = 1

    private val questionMock = mockk<OpexamsQuestion>(relaxed = true) {
        every { question } returns "Question?"
        every { answer } returns "correct"
        every { options } returns listOf("correct", "bad")
    }

    private val nodeDTO: NodesDTO = mockk(relaxed = true) {
        every { id } returns nodeId
        every { description } returns "here is some link: https://github.com"
    }
    private val emptyNodeDTO: NodesDTO = mockk(relaxed = true) {
        every { id } returns nodeId
        every { description } returns ""
    }

    private val mapDto: SummaryEditSelectMapDTO = mockk(relaxed = true) {
        every { id } returns mapId
        every { nodes } returns listOf(nodeDTO)
        every { questions } returns emptyList()
        every { answers } returns emptyList()
    }
    private val emptyMapDto: SummaryEditSelectMapDTO = mockk(relaxed = true) {
        every { nodes } returns listOf(emptyNodeDTO)
    }

    @BeforeTest
    fun setUp() {
        mapsDataSource = mockk(relaxed = true)
        yandex300DataSource = mockk(relaxed = true)
        opexamsDataSource = mockk(relaxed = true)
        nodesDataSource = mockk(relaxed = true)
        testsRepository = TestsRepository(mapsDataSource, yandex300DataSource, opexamsDataSource, nodesDataSource)
    }

    @Test
    fun testIsEnabledInteractForUserByTestIdWhenCalledThenReturnsExpectedResult() = testApplication {
        val testId = UUID.randomUUID()
        val userId = 1
        val expectedResult = true

        every({ mapsDataSource.isEnabledInteractForUserByTestId(testId, userId) }).returns(expectedResult)

        val result = testsRepository.isEnabledInteractForUserByTestId(testId, userId)

        verify {
            (mapsDataSource).isEnabledInteractForUserByTestId(testId, userId)
        }
        Assert.assertEquals(expectedResult, result)
    }

    @Test
    fun `testIsEnabledInteractForUserByTestIdWhenDataSourceThrowsExceptionThenThrowsException`() = testApplication {
        val testId = UUID.randomUUID()
        val userId = 1

        every({ mapsDataSource.isEnabledInteractForUserByTestId(testId, userId) }).throws(RuntimeException())

        assertThrows(RuntimeException::class.java) {
            testsRepository.isEnabledInteractForUserByTestId(testId, userId)
        }

        verify { (mapsDataSource).isEnabledInteractForUserByTestId(testId, userId) }
    }

    @Test
    fun testSelectFetchIdForTestWhenValidTestIdAndUserIdThenReturnsCorrectFetchId() = runBlocking {
        val testId = UUID.randomUUID()
        val userId = 1
        val expectedFetchId = UUID.randomUUID()

        every({ mapsDataSource.selectMapFetchIdForTest(testId, userId) }).returns(expectedFetchId)

        val actualFetchId = testsRepository.selectFetchIdForTest(testId, userId)

        assertEquals(expectedFetchId, actualFetchId)
    }

    @Test
    fun testSelectFetchIdForTestWhenInvalidTestIdOrUserIdThenReturnsNull() = runBlocking {
        val testId = UUID.randomUUID()
        val userId = 1

        every({ mapsDataSource.selectMapFetchIdForTest(testId, userId) }).returns(null)

        val actualFetchId = testsRepository.selectFetchIdForTest(testId, userId)

        assertNull(actualFetchId)
    }

    // generate

    @Test
    fun testGenerateTestWhenUserHasNoAccessThenThrowException() = testApplication {
        val userId = 1

        every({ mapsDataSource.getEditableMapIdEditForUserByNodeId(nodeId, userId) }).returns(null)

        assertThrows<IllegalArgumentException> {
            testsRepository.generateTest(userId, nodeId)
        }
    }

    @Test
    fun testGenerateTestWhenNodeDescriptionEmptyThenThrowException() = testApplication {
        val userId = 1
        val mapId = 1

        every({ mapsDataSource.getEditableMapIdEditForUserByNodeId(nodeId, userId) }).returns(mapId)
        every({ mapsDataSource.fetchEditSummary(mapId) }).returns(emptyMapDto)

        assertThrows<IllegalArgumentException> {
            testsRepository.generateTest(userId, nodeId)
        }
    }

    @Test
    fun testGenerateTestWhenDescriptionIsNullThenThrowException() = testApplication {
        val userId = 1
        val mapId = 1
        val nodeDTO: NodesDTO = mockk(relaxed = true) {
            every { id } returns nodeId
            every { description } returns null
        }

        val mapDto: SummaryEditSelectMapDTO = mockk(relaxed = true) {
            every { nodes } returns listOf(nodeDTO)
            every { questions } returns emptyList()
            every { answers } returns emptyList()
        }
        every({ mapsDataSource.getEditableMapIdEditForUserByNodeId(nodeId, userId) }).returns(mapId)
        every({ mapsDataSource.fetchEditSummary(mapId) }).returns(mapDto)

        assertThrows<IllegalArgumentException> {
            testsRepository.generateTest(userId, nodeId)
        }
    }

    @Test
    fun testGenerateTestSuccess() = testApplication {
        val userId = 1
        val mapId = 1
        every({ mapsDataSource.getEditableMapIdEditForUserByNodeId(nodeId, userId) }).returns(mapId)
        every({ mapsDataSource.fetchEditSummary(mapId) }).returns(mapDto)
        coEvery { opexamsDataSource.getSummarization(any()) } returns listOf(questionMock)

        val result = testsRepository.generateTest(userId, nodeId)

        assertTrue(result.questions.isNotEmpty())
    }

    @Test
    fun testGenerateYandexGetUrlCorrect() = testApplication {
        val userId = 1
        val mapId = 1
        every({ mapsDataSource.getEditableMapIdEditForUserByNodeId(nodeId, userId) }).returns(mapId)
        every({ mapsDataSource.fetchEditSummary(mapId) }).returns(mapDto)
        coEvery { opexamsDataSource.getSummarization(any()) } returns listOf(questionMock)
        coEvery { yandex300DataSource.getPage(any()) } returns ""

        val result = testsRepository.generateTest(userId, nodeId)

        assertTrue(result.questions.isNotEmpty())
    }

    @Test
    fun testGenerateYandexGetUrlIncorrect() = testApplication {
        val userId = 1
        val mapId = 1
        every({ mapsDataSource.getEditableMapIdEditForUserByNodeId(nodeId, userId) }).returns(mapId)
        every({ mapsDataSource.fetchEditSummary(mapId) }).returns(mapDto)
        coEvery { opexamsDataSource.getSummarization(any()) } returns listOf(questionMock)
        coEvery { yandex300DataSource.getPage(any()) } throws IllegalArgumentException()

        val result = testsRepository.generateTest(userId, nodeId)

        assertTrue(result.questions.isNotEmpty())
    }

    @Test
    fun `completeTest successfully completes a test`() {
        val userId = 1
        val testId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        val answerId = UUID.randomUUID()
        val usersTestAnswers = TestingCompleteRequestRemote(
            questions = listOf(
                QuestionCompleteRequestRemote(
                    questionId = questionId.toString(), answers = listOf(
                        AnswerCompleteRequestRemote(answerId.toString())
                    )
                )
            )
        )
        val stampedQuestionDTO = QuestionsDTO(
            id = questionId,
            testId = testId,
            questionText = "Qestion?",
            type = QuestionType.SINGLE_CHOICE,
        )

        val stampedAnswerDTO = AnswersDTO(
            id = answerId,
            questionId = questionId,
            answerText = "answer",
            isCorrect = true
        )
        val stampedTestDTO = StampedTestDTO(
            fetchId = UUID.randomUUID(),
            stampedQuestions = listOf(stampedQuestionDTO),
            stampedAnswers = listOf(stampedAnswerDTO)
        )
        every(
            { mapsDataSource.fetchTestByLastActiveStamp(userId, testId) }).returns(stampedTestDTO)

        every { nodesDataSource.sendAnswersForTest(any()) } returns emptyList()

        val result = testsRepository.completeTest(userId, testId, usersTestAnswers)

        assertNotNull(result)
        verify { (mapsDataSource).fetchTestByLastActiveStamp(userId, testId) }
    }

    @Test
    fun `completeTest error when not same size`() {
        val userId = 1
        val testId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        val answerId = UUID.randomUUID()
        val usersTestAnswers = TestingCompleteRequestRemote(
            questions = listOf(
                QuestionCompleteRequestRemote(
                    questionId = questionId.toString(), answers = emptyList()
                )
            )
        )
        val stampedQuestionDTO = QuestionsDTO(
            id = questionId,
            testId = testId,
            questionText = "Qestion?",
            type = QuestionType.SINGLE_CHOICE,
        )

        val stampedAnswerDTO = AnswersDTO(
            id = answerId,
            questionId = questionId,
            answerText = "answer",
            isCorrect = true
        )
        val stampedTestDTO = StampedTestDTO(
            fetchId = UUID.randomUUID(),
            stampedQuestions = listOf(stampedQuestionDTO),
            stampedAnswers = listOf(stampedAnswerDTO)
        )
        every { mapsDataSource.fetchTestByLastActiveStamp(userId, testId) }.returns(stampedTestDTO)

        every { nodesDataSource.sendAnswersForTest(any()) } returns emptyList()

        assertThrows<IllegalArgumentException>("Require same size of questions") {
            testsRepository.completeTest(userId, testId, usersTestAnswers)
        }
    }

    @Test
    fun `completeTest error when incorrect questionId`() {
        val userId = 1
        val testId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        val incorrectQuestionId = UUID.randomUUID()
        val answerId = UUID.randomUUID()
        val usersTestAnswers = TestingCompleteRequestRemote(
            questions = listOf(
                QuestionCompleteRequestRemote(
                    questionId = incorrectQuestionId.toString(),
                    answers = listOf(AnswerCompleteRequestRemote(answerId.toString()))
                )
            )
        )
        val stampedQuestionDTO = QuestionsDTO(
            id = questionId,
            testId = testId,
            questionText = "Qestion?",
            type = QuestionType.SINGLE_CHOICE,
        )

        val stampedAnswerDTO = AnswersDTO(
            id = answerId,
            questionId = questionId,
            answerText = "answer",
            isCorrect = true
        )
        val stampedTestDTO = StampedTestDTO(
            fetchId = UUID.randomUUID(),
            stampedQuestions = listOf(stampedQuestionDTO),
            stampedAnswers = listOf(stampedAnswerDTO)
        )
        every { mapsDataSource.fetchTestByLastActiveStamp(userId, testId) }.returns(stampedTestDTO)

        every { nodesDataSource.sendAnswersForTest(any()) } returns emptyList()

        assertThrows<IllegalArgumentException> {
            testsRepository.completeTest(userId, testId, usersTestAnswers)
        }
    }

    @Test
    fun `completeTest error when incorrect answerId`() {
        val userId = 1
        val testId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        val incorrectAnswerId = UUID.randomUUID()
        val answerId = UUID.randomUUID()
        val usersTestAnswers = TestingCompleteRequestRemote(
            questions = listOf(
                QuestionCompleteRequestRemote(
                    questionId = questionId.toString(),
                    answers = listOf(AnswerCompleteRequestRemote(incorrectAnswerId.toString()))
                )
            )
        )
        val stampedQuestionDTO = QuestionsDTO(
            id = questionId,
            testId = testId,
            questionText = "Qestion?",
            type = QuestionType.SINGLE_CHOICE,
        )

        val stampedAnswerDTO = AnswersDTO(
            id = answerId,
            questionId = questionId,
            answerText = "answer",
            isCorrect = true
        )
        val stampedTestDTO = StampedTestDTO(
            fetchId = UUID.randomUUID(),
            stampedQuestions = listOf(stampedQuestionDTO),
            stampedAnswers = listOf(stampedAnswerDTO)
        )
        every { mapsDataSource.fetchTestByLastActiveStamp(userId, testId) }.returns(stampedTestDTO)

        every { nodesDataSource.sendAnswersForTest(any()) } returns emptyList()

        assertThrows<IllegalArgumentException>() {
            testsRepository.completeTest(userId, testId, usersTestAnswers)
        }
    }

    @Test
    fun `completeTest throws IllegalArgumentException when questions are empty`() {
        val userId = 1
        val testId = UUID.randomUUID()
        val usersTestAnswers = TestingCompleteRequestRemote(questions = emptyList())

        val exception = assertThrows<IllegalArgumentException> {
            testsRepository.completeTest(userId, testId, usersTestAnswers)
        }

        assertEquals("Questions field is empty", exception.message)
    }

    @Test
    fun `completeTest throws IllegalArgumentException when a question has no answers`() {
        val userId = 1
        val testId = UUID.randomUUID()
        val usersTestAnswers = TestingCompleteRequestRemote(
            questions = listOf(
                QuestionCompleteRequestRemote(questionId = UUID.randomUUID().toString(), answers = emptyList())
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            testsRepository.completeTest(userId, testId, usersTestAnswers)
        }

        assertEquals("Some questions sended without answer", exception.message)
    }
}
