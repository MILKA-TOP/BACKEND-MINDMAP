package mmap.domain.maps

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import mmap.data.maps.MapsDataSource
import mmap.database.answers.AnswersDTO
import mmap.database.extensions.UpdateRowDTO
import mmap.database.maps.SummaryEditSelectMapDTO
import mmap.database.nodes.NodesDTO
import mmap.database.questions.QuestionType
import mmap.database.questions.QuestionsDTO
import mmap.database.tests.TestsDTO
import mmap.domain.maps.models.request.*
import java.util.*
import kotlin.coroutines.coroutineContext

class MapsEditRepository(
    private val mapsDataSource: MapsDataSource
) {
    fun selectAdminId(mapId: Int) = mapsDataSource.selectAdminId(mapId)

    suspend fun update(mapId: Int, updatedParams: MapsUpdateRequestParams) {

        val scope = CoroutineScope(coroutineContext)

        val savedMap = mapsDataSource.fetchEditSummary(mapId)
        val nodesIdsMap = createUuidIds(updatedParams.nodes.insert) { it.nodeId }.toMutableMap()
        val testsIdsMap = createUuidIds(updatedParams.tests.insert) { it.testId }.toMutableMap()
        val questionsIdsMap = createUuidIds(updatedParams.questions.insert) { it.questionId }.toMutableMap()
        val answersIdsMap = createUuidIds(updatedParams.answers.insert) { it.answerId }.toMutableMap()

        nodesIdsMap.putAll(savedMap.nodes.associate { it.id.toString() to it.id })
        testsIdsMap.putAll(savedMap.tests.associate { it.id.toString() to it.id })
        questionsIdsMap.putAll(savedMap.questions.associate { it.id.toString() to it.id })
        answersIdsMap.putAll(savedMap.answers.associate { it.id.toString() to it.id })

        val nodesDTOAsync = scope.async {
            updatedParams.nodes.toNodesDTO(
                savedMap = savedMap,
                nodesIdsMap = nodesIdsMap,
            )
        }
        val testsDTOAsync = scope.async {
            updatedParams.tests.toTestsDTO(
                savedMap = savedMap,
                nodesIdsMap = nodesIdsMap,
                testIdsMap = testsIdsMap,
            )
        }
        val questionsDTOAsync = scope.async {
            updatedParams.questions.toQuestionsDTO(
                savedMap = savedMap,
                testsIdsMap = testsIdsMap,
                questionIdsMap = questionsIdsMap,
            )
        }
        val answersDTOAsync = scope.async {
            updatedParams.answers.toAnswersDTO(
                savedMap = savedMap,
                answerIdsMap = answersIdsMap,
                questionIdsMap = questionsIdsMap,
            )
        }
        val nodesDTO = nodesDTOAsync.await()
        val testsDTO = testsDTOAsync.await()
        val questionsDTO = questionsDTOAsync.await()
        val answersDTO = answersDTOAsync.await()

        checkSingleRowInteraction(nodesDTO) { it.id }
        checkSingleRowInteraction(questionsDTO) { it.id }
        checkSingleRowInteraction(answersDTO) { it.id }

        checkQuestionsAndAnswers(
            savedMap = savedMap,
            questionsDTO = questionsDTO,
            answersDTO = answersDTO,
            testsDTO = testsDTO,
        )

        checkSavingParentNode(
            savedMap = savedMap,
            nodesDTO = nodesDTO,
        )

        // :TODO require, that parentNodeId changed of removed node's children

        mapsDataSource.updateSummaryMap(
            mapId = mapId,
            title = updatedParams.title,
            description = updatedParams.description,
            nodes = nodesDTO,
            tests = testsDTO,
            questions = questionsDTO,
            answers = answersDTO,
        )
    }

    private fun checkSavingParentNode(
        savedMap: SummaryEditSelectMapDTO,
        nodesDTO: UpdateRowDTO<NodesDTO, UUID>
    ) {
        val parentNode = savedMap.nodes.first { it.parentNodeId == null }
        require(parentNode.id !in nodesDTO.remove)
    }

    private fun UpdatedListComponent<NodesUpdateParam, String>.toNodesDTO(
        savedMap: SummaryEditSelectMapDTO,
        nodesIdsMap: Map<String, UUID>,
    ): UpdateRowDTO<NodesDTO, UUID> {
        val savedNodeIds = savedMap.nodes.map { it.id.toString() }

        require(removed.all { it in savedNodeIds }, { "Excepted actual remove node's id" })
        require(updated.all { it.nodeId in savedNodeIds }, { "Excepted actual update node's id" })
        require(insert.all { it.parentNodeId in savedNodeIds }, { "Excepted actual insert parent-node's id" })
        require(updated.all { it.parentNodeId in savedNodeIds }, { "Excepted actual update parent-node's id" })
        require((updated + insert).all { it.mapId.toInt() == savedMap.id }, { "Node have incorrect mapId" })

        return UpdateRowDTO(
            insert = insert.map { node ->
                require(node.parentNodeId!!.isNotEmpty())
                NodesDTO(
                    id = nodesIdsMap[node.nodeId]!!,
                    mapId = node.mapId.toInt(),
                    label = node.title,
                    description = node.details,
                    parentNodeId = nodesIdsMap[node.parentNodeId]!!,
                    priorityPosition = node.priorityNumber
                )
            },
            update = updated.map { node ->
                require(node.parentNodeId!!.isNotEmpty())
                NodesDTO(
                    id = UUID.fromString(node.nodeId),
                    mapId = node.mapId.toInt(),
                    label = node.title,
                    description = node.details,
                    parentNodeId = nodesIdsMap[node.parentNodeId]!!,
                    priorityPosition = node.priorityNumber
                )
            },
            remove = removed.map(UUID::fromString)
        )
    }

    private fun UpdatedListComponent<TestUpdateParam, String>.toTestsDTO(
        savedMap: SummaryEditSelectMapDTO,
        testIdsMap: Map<String, UUID>,
        nodesIdsMap: Map<String, UUID>,
    ): UpdateRowDTO<TestsDTO, UUID> {
        val savedNodesToTests = savedMap.tests.groupBy { it.nodeId }

        require(removed.isEmpty(), { "Excepted, that you can't remove tests from node" })
        require(updated.isEmpty(), { "Excepted, that you can't update tests from node" })
        require(insert.all { it.nodeId in nodesIdsMap }, { "Excepted actual insert node's id" })
        require(
            insert.all { nodesIdsMap[it.nodeId] !in savedNodesToTests },
            { "You try add test for node with actually created test" })

        return UpdateRowDTO(
            insert = insert.map { test ->
                TestsDTO(
                    id = testIdsMap[test.testId]!!,
                    nodeId = nodesIdsMap[test.nodeId]!!,
                )
            },
        )
    }

    private fun UpdatedListComponent<QuestionUpdateParam, String>.toQuestionsDTO(
        savedMap: SummaryEditSelectMapDTO,
        questionIdsMap: Map<String, UUID>,
        testsIdsMap: Map<String, UUID>,
    ): UpdateRowDTO<QuestionsDTO, UUID> {
        val savedQuestionIds = savedMap.questions.map { it.id.toString() }

        require(removed.all { it in savedQuestionIds }, { "Excepted actual remove question's id" })
        require(updated.all { it.questionId in savedQuestionIds }, { "Excepted actual update question's id" })
        require(insert.all { it.testId in testsIdsMap }, { "Excepted actual insert tests's id" })
        require(updated.all { it.testId in testsIdsMap }, { "Excepted actual update tests's id" })
        require((insert + updated).all { it.title.trim().isNotEmpty() }, { "Excepted not empty title of question" })
        // TODO: require, that nodeId doesn't update

        return UpdateRowDTO(
            insert = insert.map { question ->
                QuestionsDTO(
                    id = questionIdsMap[question.questionId]!!,
                    testId = testsIdsMap[question.testId]!!,
                    questionText = question.title.trim(),
                    type = question.questionType,
                )
            },
            update = updated.map { question ->
                QuestionsDTO(
                    id = UUID.fromString(question.questionId),
                    testId = testsIdsMap[question.testId]!!,
                    questionText = question.title.trim(),
                    type = question.questionType,
                )
            },
            remove = removed.map(UUID::fromString)
        )
    }

    private fun UpdatedListComponent<AnswerUpdateParam, String>.toAnswersDTO(
        savedMap: SummaryEditSelectMapDTO,
        answerIdsMap: Map<String, UUID>,
        questionIdsMap: Map<String, UUID>,
    ): UpdateRowDTO<AnswersDTO, UUID> {
        val savedAnswersIds = savedMap.answers.map { it.id.toString() }

        require(removed.all { it in savedAnswersIds }, { "Excepted actual remove answer's id" })
        require(updated.all { it.answerId in savedAnswersIds }, { "Excepted actual update answer's id" })
        require(insert.all { it.questionId in questionIdsMap }, { "Excepted actual insert question's id" })
        require(updated.all { it.questionId in questionIdsMap }, { "Excepted actual update question's id" })
        require((insert + updated).all { it.title.trim().isNotEmpty() }, { "Excepted not empty title of answer" })

        return UpdateRowDTO(
            insert = insert.map { answer ->
                AnswersDTO(
                    id = answerIdsMap[answer.answerId]!!,
                    questionId = questionIdsMap[answer.questionId]!!,
                    answerText = answer.title.trim(),
                    isCorrect = answer.isCorrect,
                )
            },
            update = updated.map { answer ->
                AnswersDTO(
                    id = UUID.fromString(answer.answerId),
                    questionId = questionIdsMap[answer.questionId]!!,
                    answerText = answer.title.trim(),
                    isCorrect = answer.isCorrect,
                )
            },
            remove = removed.map(UUID::fromString)
        )
    }

    private fun checkQuestionsAndAnswers(
        questionsDTO: UpdateRowDTO<QuestionsDTO, UUID>,
        answersDTO: UpdateRowDTO<AnswersDTO, UUID>,
        testsDTO: UpdateRowDTO<TestsDTO, UUID>,
        savedMap: SummaryEditSelectMapDTO,
    ) {
        val savedTests = savedMap.tests
        val savedQuestions = savedMap.questions
        val savedAnswers = savedMap.answers

        val filteredQuestions = savedQuestions.filter { it.id !in questionsDTO.remove }
        val filteredAnswers = savedAnswers.filter { it.id !in answersDTO.remove }

        val tests =
            (savedTests.map { TestModel(testId = it.id, nodeId = it.nodeId) } +
                    testsDTO.insert.map { TestModel(testId = it.id, nodeId = it.nodeId) })
                .associateBy { it.testId }.toMutableMap()

        // saved questions model
        val questions = filteredQuestions.associate {
            it.id to QuestionModel(
                testId = it.testId,
                questionId = it.id,
                text = it.questionText,
                type = it.type,
                answers = emptyList()
            )
        }.toMutableMap()

        filteredAnswers.map {
            val question = questions[it.questionId]
            if (question != null) {
                questions[it.questionId] = question.copy(
                    answers = question.answers + AnswerModel(
                        answerId = it.id,
                        text = it.answerText,
                        isCorrect = it.isCorrect
                    )
                )
            }
        }

        // inserted questions
        questionsDTO.insert.map {
            it.id to QuestionModel(
                testId = it.testId,
                questionId = it.id,
                text = it.questionText,
                type = it.type,
                answers = emptyList()
            )
        }.let(questions::putAll)
        // updated questions
        questionsDTO.update.map {
            val question = questions[it.id]!!

            questions[it.id] = question.copy(
                text = it.questionText,
                type = it.type,
            )
        }

        // inserted answers
        answersDTO.insert.map {
            val question = questions[it.questionId]!!
            questions[it.questionId] = question.copy(
                answers = question.answers + AnswerModel(
                    answerId = it.id,
                    text = it.answerText,
                    isCorrect = it.isCorrect
                )
            )
        }

        // updated answers
        answersDTO.update.map {
            val question = questions[it.questionId]!!
            val updatedAnswers = question.answers.map { answer ->
                if (answer.answerId == it.id) {
                    answer.copy(
                        text = answer.text,
                        isCorrect = answer.isCorrect,
                    )
                } else answer
            }
            questions[it.questionId] = question.copy(answers = updatedAnswers)
        }

        questions.values.map {
            val test = tests[it.testId]!!
            tests[it.testId] = test.copy(questions = test.questions + it)
        }

        require(
            questions.all { (_, question) ->
                when (question.type) {
                    QuestionType.SINGLE_CHOICE -> question.answers.count { it.isCorrect } == 1
                    QuestionType.MULTIPLE_CHOICE -> question.answers.count { it.isCorrect } >= 1
                }
            }
        )
        require(
            tests.values.all { it.questions.isNotEmpty() },
            { "Can't set empty tests for node" }
        )
    }

    private fun <T, K> checkSingleRowInteraction(dto: UpdateRowDTO<T, K>, getKey: (T) -> K) {
        val idsList = dto.remove + dto.update.map(getKey) + dto.insert.map(getKey)
        val idsSet = idsList.toSet()

        require(idsList.size == idsSet.size)
    }

    private fun <T> createUuidIds(list: List<T>, getId: (T) -> String): Map<String, UUID> =
        list.map(getId).associateWith { UUID.randomUUID() }

    private data class TestModel(
        val testId: UUID,
        val nodeId: UUID,
        val questions: List<QuestionModel> = emptyList(),
    )

    private data class QuestionModel(
        val testId: UUID,
        val questionId: UUID,
        val text: String,
        val type: QuestionType,
        val answers: List<AnswerModel>
    )

    private data class AnswerModel(
        val answerId: UUID,
        val text: String,
        val isCorrect: Boolean
    )
}
