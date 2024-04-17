package mmap.database.questionsevents

import kotlinx.serialization.Serializable
import mmap.database.questions.QuestionType

@Serializable
sealed class QuestionsStateJson {
    @Serializable
    data object Remove : QuestionsStateJson()

    @Serializable
    data class Insert(
        val text: String,
        val type: QuestionType,
    ) : QuestionsStateJson()

    @Serializable
    data class Update(
        val text: String,
        val type: QuestionType,
    ) : QuestionsStateJson()
}
