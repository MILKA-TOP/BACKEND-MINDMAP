package mmap.features.testing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import mmap.core.buildTestResultViewResponse
import mmap.database.answerprogress.AnswerProgress
import mmap.database.answerprogress.AnswersProgressDTO
import mmap.database.extensions.UpdateRowDTO
import mmap.database.extensions.selectFetchIdForTest
import mmap.database.maps.Maps
import mmap.database.selectedmaps.SelectedMaps
import mmap.database.selectedmaps.SelectedMaps.getEditableMapIdEditForUserByNodeId
import mmap.database.tests.Tests
import mmap.database.tests.TestsDTO
import mmap.features.maps.models.response.AnswersEditResponseRemote.Companion.toEditDomainModel
import mmap.features.maps.models.response.QuestionsEditResponseRemote.Companion.toEditDomainModel
import mmap.features.maps.models.response.TestsEditResponseRemote.Companion.toEditDomainModel
import mmap.features.testing.models.request.TestingCompleteRequestRemote
import java.util.*

class TestingController(private val call: ApplicationCall) {

    private val linkRegex =
        Regex("(http|ftp|https):\\/\\/([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:\\/~+#-]*[\\w@?^=%&\\/~+#-])")

    suspend fun completeTest(userId: String, testId: String) {
        val userIdInt = userId.toInt()
        val testIdUuid = UUID.fromString(testId)
        val usersTestAnswers = call.receive<TestingCompleteRequestRemote>()

        val isNodeEnabled = SelectedMaps.isEnabledInteractForUserByTestId(testIdUuid, userIdInt)

        if (isNodeEnabled) {
            val lastTestsFetchId = selectFetchIdForTest(testIdUuid, userIdInt)
            if (lastTestsFetchId == null) {
                require(usersTestAnswers.questions.isNotEmpty(), { "Questions field is empty" })
                require(
                    usersTestAnswers.questions.all { it.answers.isNotEmpty() },
                    { "Some questions sended without answer" })

                val stampedTest = Tests.fetchTestByLastActiveStamp(userIdInt, testIdUuid)
                val stampedQuestionsIds = stampedTest.stampedQuestions.map { it.id }
                val stampedAnswersIds = stampedTest.stampedAnswers.map { it.id }


                val receivedQuestionsIds = usersTestAnswers.questions.map { UUID.fromString(it.questionId) }
                val receivedSelectedAnswersIds =
                    usersTestAnswers.questions.flatMap { it.answers }.map { UUID.fromString(it.answerId) }

                require(usersTestAnswers.questions.size == stampedTest.stampedQuestions.size,
                    { "Require same size of questions" })
                require(receivedQuestionsIds.all { it in stampedQuestionsIds })
                require(receivedSelectedAnswersIds.all { it in stampedAnswersIds })

                val answerProgressDTOs = receivedSelectedAnswersIds.map {
                    AnswersProgressDTO(
                        userId = userIdInt,
                        answerId = it,
                        fetchId = stampedTest.fetchId,
                    )
                }
                AnswerProgress.addAnswers(answerProgressDTOs)

                val testResultViewModel = buildTestResultViewResponse(
                    selectedAnswers = stampedAnswersIds,
                    stampedAnswers = stampedTest.stampedAnswers,
                    stampedQuestions = stampedTest.stampedQuestions,
                    testsIds = listOf(testIdUuid),
                )[testIdUuid]!!

                call.respond(HttpStatusCode.OK, testResultViewModel)
            } else {
                call.respond(HttpStatusCode.Conflict, "You can't complete this test again")
            }
        } else {
            call.respond(HttpStatusCode.Conflict, "You doesn't have access for this node and map")
        }
    }

    suspend fun generateTest(userId: Int, nodeId: UUID) {
        val mapId = getEditableMapIdEditForUserByNodeId(nodeId, userId)
        if (mapId == null) {
            call.respond(HttpStatusCode.Conflict, "You doesn't have access for this node and map")
            return
        }

        val mapDto = Maps.fetchEditSummary(mapId)
        val node = mapDto.nodes.first { it.id == nodeId }

        val nodeDescriptionOriginal = node.description

        if (nodeDescriptionOriginal.isNullOrEmpty()) {
            call.respond(HttpStatusCode.Conflict, "Node description is incorrect")
            return
        }

        var nodeDescriptionResult: String = nodeDescriptionOriginal
        val links = linkRegex.findAll(node.description)

        links.forEach { link ->
            val description = yandexRepository.getLinkDescription(link.value)

            description?.let {
                nodeDescriptionResult = nodeDescriptionResult.replace(
                    oldValue = link.value,
                    newValue = "${link.value} ${it}"
                )
            }
        }
        println(nodeDescriptionResult)

        val test = mapDto.tests.firstOrNull { it.nodeId == nodeId }
        val currentTestDto = test ?: TestsDTO(id = UUID.randomUUID(), nodeId = nodeId)

        val generatedModel = opexamsRepository.generate(currentTestDto.id, nodeDescriptionResult)
        val testInsertDto = if (test == null) listOf(currentTestDto) else emptyList()

        Maps.updateSummaryMap(
            mapId = mapId,
            title = mapDto.title,
            description = mapDto.description,
            nodes = UpdateRowDTO(),
            tests = UpdateRowDTO(insert = testInsertDto),
            questions = UpdateRowDTO(insert = generatedModel.questions),
            answers = UpdateRowDTO(insert = generatedModel.answers)
        )

        val answersModels = (mapDto.answers + generatedModel.answers).map { it.toEditDomainModel() }.groupBy { it.questionId }
        val questionsModels =
            (mapDto.questions + generatedModel.questions).map {
                it.toEditDomainModel(answersModels[it.id.toString()]!!)
            }.groupBy { it.testId }
        val testModels = currentTestDto.toEditDomainModel(questionsModels[currentTestDto.id.toString()]!!)

        call.respond(HttpStatusCode.OK, testModels)
    }

}