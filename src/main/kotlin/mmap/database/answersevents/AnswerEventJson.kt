package mmap.database.answersevents

import kotlinx.serialization.Serializable

@Serializable
sealed class AnswerEventJson {
    @Serializable
    data object Remove : AnswerEventJson()

    @Serializable
    data class Insert(
        val text: String,
        val isCorrect: Boolean,
    ) : AnswerEventJson()

    @Serializable
    data class Update(
        val text: String,
        val isCorrect: Boolean,
    ) : AnswerEventJson()
}
