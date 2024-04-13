package mmap.database.answers

import java.util.UUID

data class AnswersDTO(
    val id: UUID,
    val questionId: UUID,
    val answerText: String,
    val isCorrect: Boolean,
    val isRemoved: Boolean = false,
)
