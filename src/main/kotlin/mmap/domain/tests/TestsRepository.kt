package mmap.domain.tests

import mmap.core.buildTestResultViewResponse
import mmap.data.maps.MapsDataSource
import mmap.data.tests.opexams.OpexamsDataSource
import mmap.data.tests.yandex.Yandex300DataSource
import mmap.database.answerprogress.AnswerProgress
import mmap.database.answerprogress.AnswersProgressDTO
import mmap.database.answers.AnswersDTO
import mmap.database.extensions.UpdateRowDTO
import mmap.database.questions.QuestionType
import mmap.database.questions.QuestionsDTO
import mmap.database.tests.TestsDTO
import mmap.domain.tests.models.TestingCompleteRequestRemote
import mmap.domain.tests.models.Yandex300Response
import mmap.domain.tests.models.opexams.QuestionGeneratedModel
import mmap.features.maps.models.response.AnswersEditResponseRemote.Companion.toEditDomainModel
import mmap.features.maps.models.response.QuestionsEditResponseRemote.Companion.toEditDomainModel
import mmap.features.maps.models.response.TestResultViewResponseRemote
import mmap.features.maps.models.response.TestsEditResponseRemote
import mmap.features.maps.models.response.TestsEditResponseRemote.Companion.toEditDomainModel
import org.jsoup.Jsoup
import java.util.*

class TestsRepository(
    private val mapsDataSource: MapsDataSource,
    private val yandex300DataSource: Yandex300DataSource,
    private val opexamsDataSource: OpexamsDataSource,
) {
    private val linkRegex =
        Regex("(http|ftp|https):\\/\\/([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:\\/~+#-]*[\\w@?^=%&\\/~+#-])")

    fun isEnabledInteractForUserByTestId(testId: UUID, userId: Int) =
        mapsDataSource.isEnabledInteractForUserByTestId(testId, userId)

    fun selectFetchIdForTest(testId: UUID, userId: Int) = mapsDataSource.selectMapFetchIdForTest(testId, userId)
    fun completeTest(
        userId: Int,
        testId: UUID,
        usersTestAnswers: TestingCompleteRequestRemote
    ): TestResultViewResponseRemote {
        require(usersTestAnswers.questions.isNotEmpty()) { "Questions field is empty" }
        require(
            usersTestAnswers.questions.all { it.answers.isNotEmpty() }
        ) { "Some questions sended without answer" }

        val stampedTest = mapsDataSource.fetchTestByLastActiveStamp(userId, testId)
        val stampedQuestionsIds = stampedTest.stampedQuestions.map { it.id }
        val stampedAnswersIds = stampedTest.stampedAnswers.map { it.id }

        val receivedQuestionsIds = usersTestAnswers.questions.map { UUID.fromString(it.questionId) }
        val receivedSelectedAnswersIds =
            usersTestAnswers.questions.flatMap { it.answers }.map { UUID.fromString(it.answerId) }

        require(
            usersTestAnswers.questions.size == stampedTest.stampedQuestions.size
        ) { "Require same size of questions" }
        require(receivedQuestionsIds.all { it in stampedQuestionsIds })
        require(receivedSelectedAnswersIds.all { it in stampedAnswersIds })

        val answerProgressDTOs = receivedSelectedAnswersIds.map {
            AnswersProgressDTO(
                userId = userId,
                answerId = it,
                fetchId = stampedTest.fetchId,
            )
        }
        AnswerProgress.addAnswers(answerProgressDTOs)

        val testResultViewModel = buildTestResultViewResponse(
            selectedAnswers = stampedAnswersIds,
            stampedAnswers = stampedTest.stampedAnswers,
            stampedQuestions = stampedTest.stampedQuestions,
            testsIds = listOf(testId),
        ).getValue(testId)

        return testResultViewModel
    }

    suspend fun generateTest(userId: Int, nodeId: UUID): TestsEditResponseRemote {
        val mapId = mapsDataSource.getEditableMapIdEditForUserByNodeId(nodeId, userId)
        requireNotNull(mapId) { "You doesn't have access for this node and map" }

        val mapDto = mapsDataSource.fetchEditSummary(mapId)
        val node = mapDto.nodes.first { it.id == nodeId }

        val nodeDescriptionOriginal = node.description
        require(!nodeDescriptionOriginal.isNullOrEmpty()) { "Node description is incorrect" }

        var nodeDescriptionResult: String = nodeDescriptionOriginal
        val links = linkRegex.findAll(node.description)

        links.forEach { link ->
            val description = getLinkDescription(link.value)

            description?.let {
                nodeDescriptionResult = nodeDescriptionResult.replace(
                    oldValue = link.value,
                    newValue = "${link.value} $it"
                )
            }
        }

        val test = mapDto.tests.firstOrNull { it.nodeId == nodeId }
        val currentTestDto = test ?: TestsDTO(id = UUID.randomUUID(), nodeId = nodeId)

        val generatedModel = generate(currentTestDto.id, nodeDescriptionResult)
        val testInsertDto = if (test == null) listOf(currentTestDto) else emptyList()

        mapsDataSource.updateSummaryMap(
            mapId = mapId,
            title = mapDto.title,
            description = mapDto.description,
            tests = UpdateRowDTO(insert = testInsertDto),
            questions = UpdateRowDTO(insert = generatedModel.questions),
            answers = UpdateRowDTO(insert = generatedModel.answers),
        )

        val answersModels =
            (mapDto.answers + generatedModel.answers).map { it.toEditDomainModel() }.groupBy { it.questionId }
        val questionsModels =
            (mapDto.questions + generatedModel.questions).map {
                it.toEditDomainModel(answersModels[it.id.toString()]!!)
            }.groupBy { it.testId }
        val testModels = currentTestDto.toEditDomainModel(questionsModels[currentTestDto.id.toString()]!!)

        return testModels
    }

    private suspend fun getLinkDescription(link: String): String? {
        val summaryLink = kotlin.runCatching {
            yandex300DataSource.getSummarization(link)
        }.getOrNull()
        return (summaryLink as? Yandex300Response.Success)?.sharing_url?.let {
            kotlin.runCatching {
                yandex300DataSource.getPage(link)
            }.getOrNull()?.let {
                Jsoup.parse(it).getElementsByTag("meta").first { it.attr("name") == "description" }.attr("content")
            }
        }
    }

    private suspend fun generate(testId: UUID, description: String): QuestionGeneratedModel {
        val questions = opexamsDataSource.getSummarization(description).orEmpty()
        val questionsDto = mutableListOf<QuestionsDTO>()
        val answersDto = mutableListOf<AnswersDTO>()

        questions.forEach { question ->
            val questionId = UUID.randomUUID()

            questionsDto.add(
                QuestionsDTO(
                    id = questionId,
                    testId = testId,
                    questionText = question.question,
                    type = QuestionType.SINGLE_CHOICE
                )
            )
            question.options.map { answer ->
                answersDto.add(
                    AnswersDTO(
                        id = UUID.randomUUID(),
                        questionId = questionId,
                        answerText = answer,
                        isCorrect = answer == question.answer
                    )
                )
            }
        }

        return QuestionGeneratedModel(
            questions = questionsDto,
            answers = answersDto,
        )
    }
}
