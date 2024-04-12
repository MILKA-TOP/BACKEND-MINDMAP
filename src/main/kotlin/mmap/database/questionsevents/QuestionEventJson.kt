package mmap.database.questionsevents

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mmap.database.questions.QuestionType

@Serializable
sealed class QuestionEventJson {
    @Serializable
    data object Remove : QuestionEventJson()

    @Serializable
    data class Insert(
        val text: String,
        val type: QuestionType,
    ) : QuestionEventJson()

    @Serializable
    data class Update(
        val text: String,
        val type: QuestionType,
    ) : QuestionEventJson()

}