package mmap.domain.maps.models.response

import kotlinx.serialization.Serializable
import mmap.database.answers.AnswersDTO
import mmap.database.nodes.NodesDTO
import mmap.database.questions.QuestionType
import mmap.database.questions.QuestionsDTO
import mmap.database.tests.TestsDTO

@Serializable
data class SummaryEditMapResponseRemote(
    val id: String,
    val title: String,
    val description: String,
    val referralId: String,
    val admin: UserResponseRemote,
    val nodes: List<NodesEditResponseRemote>,
    val type: MapActionType = MapActionType.EDIT,
    val accessUsers: List<UserResponseRemote> = emptyList()
)

@Serializable
data class NodesEditResponseRemote(
    val id: String,
    val label: String,
    val description: String,
    val priorityPosition: Int,
    val parentNodeId: String? = null,
    val test: TestsEditResponseRemote? = null,
) {
    companion object {
        fun NodesDTO.toEditDomainModel(test: TestsEditResponseRemote?) = NodesEditResponseRemote(
            id = id.toString(),
            label = label,
            description = description.orEmpty(),
            parentNodeId = parentNodeId?.toString(),
            test = test,
            priorityPosition = priorityPosition,
        )
    }
}

@Serializable
data class TestsEditResponseRemote(
    val id: String,
    val nodeId: String,
    val questions: List<QuestionsEditResponseRemote> = emptyList(),
) {
    companion object {
        fun TestsDTO.toEditDomainModel(questions: List<QuestionsEditResponseRemote>) = TestsEditResponseRemote(
            id = id.toString(),
            nodeId = nodeId.toString(),
            questions = questions,
        )
    }
}

@Serializable
data class QuestionsEditResponseRemote(
    val id: String,
    val testId: String,
    val questionText: String,
    val questionType: QuestionType,
    val answers: List<AnswersEditResponseRemote> = emptyList(),
) {
    companion object {
        fun QuestionsDTO.toEditDomainModel(answers: List<AnswersEditResponseRemote>) = QuestionsEditResponseRemote(
            id = id.toString(),
            testId = testId.toString(),
            questionText = questionText,
            questionType = type,
            answers = answers
        )
    }
}

@Serializable
data class AnswersEditResponseRemote(
    val id: String,
    val questionId: String,
    val answerText: String,
    val isCorrect: Boolean,
) {
    companion object {
        fun AnswersDTO.toEditDomainModel() = AnswersEditResponseRemote(
            id = id.toString(),
            questionId = questionId.toString(),
            answerText = answerText,
            isCorrect = isCorrect,
        )
    }
}
