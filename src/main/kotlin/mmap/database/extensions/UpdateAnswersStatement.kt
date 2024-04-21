package mmap.database.extensions

import mmap.database.answers.Answers
import mmap.database.answers.AnswersDTO
import mmap.database.answersstates.AnswerStates
import java.util.*

fun updateAnswersStatement(
    answers: UpdateRowDTO<AnswersDTO, UUID>
) {
    Answers.batchInsertStatement(answers.insert)
    AnswerStates.batchInsertCreateEventsStatement(answers.insert)

    AnswerStates.batchInsertUpdateEventsStatement(answers.update)

    AnswerStates.batchInsertRemoveEventsStatement(answers.remove)
}
