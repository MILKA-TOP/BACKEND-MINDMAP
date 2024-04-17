package mmap.database.answersstates

import kotlinx.serialization.Serializable

@Serializable
sealed class AnswerStateJson {
    @Serializable
    data object Remove : AnswerStateJson()

    @Serializable
    data class Insert(
        val text: String,
        val isCorrect: Boolean,
    ) : AnswerStateJson()

    @Serializable
    data class Update(
        val text: String,
        val isCorrect: Boolean,
    ) : AnswerStateJson()
}
