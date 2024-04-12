package mmap.core

import mmap.database.answers.AnswersDTO
import mmap.database.questions.QuestionsDTO
import mmap.features.maps.models.response.AnswersResultResponseRemote.Companion.toResultDomainModel
import mmap.features.maps.models.response.QuestionsResultResponseRemote.Companion.toResultDomainModel
import mmap.features.maps.models.response.TestResultViewResponseRemote
import java.util.*

fun buildTestResultViewResponse(
    selectedAnswers: List<UUID>,
    stampedAnswers: List<AnswersDTO>,
    stampedQuestions: List<QuestionsDTO>,
    testsIds: List<UUID>,
): Map<UUID, TestResultViewResponseRemote> {
    val stampedAnswersModels = stampedAnswers
        .map { it.toResultDomainModel(it.id in selectedAnswers) }
        .groupBy { it.questionId }

    val stampedQuestions =
        stampedQuestions.map {
            it.toResultDomainModel(stampedAnswersModels[it.id.toString()]!!)
        }.groupBy { it.testId }

    return testsIds.mapNotNull { testId ->
        stampedQuestions[testId.toString()]?.let { questions ->
            val passedQuestionsCount = questions.count { question ->
                question.answers.all { it.isCorrect == it.isSelected }
            }
            testId to TestResultViewResponseRemote(
                correctQuestionsCount = passedQuestionsCount,
                completedQuestions = questions,
            )
        }
    }.toMap()
}
