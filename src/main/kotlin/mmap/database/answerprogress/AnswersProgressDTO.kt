package mmap.database.answerprogress

import java.util.UUID

data class AnswersProgressDTO(
    val userId: Int,
    val answerId: UUID,
    val fetchId: UUID
)