package mmap.database.tests

import mmap.database.answers.AnswersDTO
import mmap.database.questions.QuestionsDTO
import java.util.UUID

data class StampedTestDTO(
    val fetchId: UUID,
    val stampedQuestions: List<QuestionsDTO>,
    val stampedAnswers: List<AnswersDTO>,
)