package mmap.database.questions

import java.util.*

data class QuestionsDTO(
    val id: UUID,
    val testId: UUID,
    val questionText: String,
    val type: QuestionType,
    val isDeleted: Boolean = false,
)
