package mmap.domain.tests.models

import kotlinx.serialization.Serializable

@Serializable
data class TestingCompleteRequestRemote(
    val questions: List<QuestionCompleteRequestRemote>
)

@Serializable
data class QuestionCompleteRequestRemote(
    val questionId: String,
    val answers: List<AnswerCompleteRequestRemote>
)

@Serializable
data class AnswerCompleteRequestRemote(
    val answerId: String,
)
