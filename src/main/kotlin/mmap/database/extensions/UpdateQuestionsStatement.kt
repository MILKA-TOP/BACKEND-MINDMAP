package mmap.database.extensions

import mmap.database.questions.Questions
import mmap.database.questions.QuestionsDTO
import mmap.database.questionsevents.QuestionStates
import java.util.*

fun updateQuestionsStatement(
    questions: UpdateRowDTO<QuestionsDTO, UUID>
) {
    Questions.batchInsertStatement(questions.insert)
    QuestionStates.batchInsertCreateEventsStatement(questions.insert)

    QuestionStates.batchInsertUpdateEventsStatement(questions.update)

    QuestionStates.batchInsertRemoveEventsStatement(questions.remove)
}
