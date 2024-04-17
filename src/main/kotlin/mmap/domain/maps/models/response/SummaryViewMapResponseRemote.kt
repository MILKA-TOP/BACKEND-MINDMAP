package mmap.domain.maps.models.response

import kotlinx.serialization.Serializable
import mmap.database.answers.AnswersDTO
import mmap.database.nodes.NodesDTO
import mmap.database.questions.QuestionType
import mmap.database.questions.QuestionsDTO
import mmap.database.tests.TestsDTO

@Serializable
data class SummaryViewMapResponseRemote(
    val id: String,
    val title: String,
    val description: String,
    val referralId: String,
    val admin: UserResponseRemote,
    val nodes: List<NodesViewResponseRemote>,
    val type: MapActionType,
)

@Serializable
data class NodesViewResponseRemote(
    val id: String,
    val label: String,
    val description: String,
    val priorityPosition: Int,
    val isSelected: Boolean = false,
    val parentNodeId: String? = null,
    val test: TestsViewResponseRemote? = null,
) {
    companion object {
        fun NodesDTO.toViewDomainModel(isSelected: Boolean, test: TestsViewResponseRemote?) = NodesViewResponseRemote(
            id = id.toString(),
            label = label,
            description = description.orEmpty(),
            parentNodeId = parentNodeId?.toString(),
            test = test,
            isSelected = isSelected,
            priorityPosition = priorityPosition,
        )
    }
}

@Serializable
data class TestsViewResponseRemote(
    val id: String,
    val nodeId: String,
    val questions: List<QuestionsViewResponseRemote> = emptyList(),
    val testResult: TestResultViewResponseRemote? = null,
) {
    companion object {
        fun TestsDTO.toViewDomainModel(
            questions: List<QuestionsViewResponseRemote>,
            testResult: TestResultViewResponseRemote? = null
        ) = TestsViewResponseRemote(
            id = id.toString(),
            nodeId = nodeId.toString(),
            questions = questions,
            testResult = testResult,
        )
    }
}

@Serializable
data class TestResultViewResponseRemote(
    val correctQuestionsCount: Int,
    val completedQuestions: List<QuestionsResultResponseRemote> = emptyList(),
    val message: String? = null,
)

@Serializable
data class QuestionsViewResponseRemote(
    val id: String,
    val testId: String,
    val questionText: String,
    val type: QuestionType,
    val answers: List<AnswersViewResponseRemote> = emptyList(),
) {
    companion object {
        fun QuestionsDTO.toViewDomainModel(answers: List<AnswersViewResponseRemote>) = QuestionsViewResponseRemote(
            id = id.toString(),
            testId = testId.toString(),
            questionText = questionText,
            type = type,
            answers = answers
        )
    }
}

@Serializable
data class AnswersViewResponseRemote(
    val id: String,
    val questionId: String,
    val answerText: String,
) {
    companion object {
        fun AnswersDTO.toViewDomainModel() = AnswersViewResponseRemote(
            id = id.toString(),
            questionId = questionId.toString(),
            answerText = answerText,
        )
    }
}

@Serializable
data class QuestionsResultResponseRemote(
    val id: String,
    val testId: String,
    val questionText: String,
    val type: QuestionType,
    val answers: List<AnswersResultResponseRemote> = emptyList(),
) {
    companion object {
        fun QuestionsDTO.toResultDomainModel(answers: List<AnswersResultResponseRemote>) = QuestionsResultResponseRemote(
            id = id.toString(),
            testId = testId.toString(),
            questionText = questionText,
            type = type,
            answers = answers
        )
    }
}

@Serializable
data class AnswersResultResponseRemote(
    val id: String,
    val questionId: String,
    val answerText: String,
    val isCorrect: Boolean,
    val isSelected: Boolean,
) {
    companion object {
        fun AnswersDTO.toResultDomainModel(isSelected: Boolean) = AnswersResultResponseRemote(
            id = id.toString(),
            questionId = questionId.toString(),
            answerText = answerText,
            isCorrect = isCorrect,
            isSelected = isSelected,
        )
    }
}
