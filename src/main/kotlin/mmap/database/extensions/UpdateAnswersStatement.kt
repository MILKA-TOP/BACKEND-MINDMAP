package mmap.database.extensions

import mmap.database.answers.Answers
import mmap.database.answers.AnswersDTO
import mmap.database.answersevents.AnswerEvents
import mmap.database.answerssnapshot.AnswersSnapshot
import java.util.*

fun updateAnswersStatement(
    answers: UpdateRowDTO<AnswersDTO, UUID>
) {
    Answers.batchInsertStatement(answers.insert)
    AnswersSnapshot.batchInsertSnapshotsStatement(answers.insert)
    AnswerEvents.batchInsertCreateEventsStatement(answers.insert)

    AnswersSnapshot.updateSnapshotStatement(answers.update)
    AnswerEvents.batchInsertUpdateEventsStatement(answers.update)

    AnswersSnapshot.removeSnapshotStatement(answers.remove)
    AnswerEvents.batchInsertRemoveEventsStatement(answers.remove)
}