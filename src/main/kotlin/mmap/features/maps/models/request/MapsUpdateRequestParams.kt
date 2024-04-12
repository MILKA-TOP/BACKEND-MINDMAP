package mmap.features.maps.models.request

import kotlinx.serialization.Serializable
import mmap.database.questions.QuestionType

@Serializable
data class MapsUpdateRequestParams(
    val title: String,
    val description: String,
    val nodes: UpdatedListComponent<NodesUpdateParam, String> = UpdatedListComponent(),
    val tests: UpdatedListComponent<TestUpdateParam, String> = UpdatedListComponent(),
    val questions: UpdatedListComponent<QuestionUpdateParam, String> = UpdatedListComponent(),
    val answers: UpdatedListComponent<AnswerUpdateParam, String> = UpdatedListComponent(),
)

@Serializable
data class NodesUpdateParam(
    val nodeId: String,
    val mapId: String,
    val parentNodeId: String? = null,
    val title: String,
    val details: String = "",
    val priorityNumber: Int,
)

@Serializable
data class TestUpdateParam(
    val testId: String,
    val nodeId: String,
)

@Serializable
data class QuestionUpdateParam(
    val questionId: String,
    val testId: String,
    val questionType: QuestionType,
    val title: String,
)

@Serializable
data class AnswerUpdateParam(
    val questionId: String,
    val answerId: String,
    val title: String,
    val isCorrect: Boolean,
)

@Serializable
data class UpdatedListComponent<T, K>(
    val insert: List<T> = emptyList(),
    val removed: List<K> = emptyList(),
    val updated: List<T> = emptyList(),
)
