package mmap.domain.tests.models.opexams

import mmap.database.answers.AnswersDTO
import mmap.database.questions.QuestionsDTO

data class QuestionGeneratedModel(
    val questions: List<QuestionsDTO>,
    val answers: List<AnswersDTO>,
)
