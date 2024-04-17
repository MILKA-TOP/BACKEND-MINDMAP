package mmap.database.extensions

import mmap.database.answers.Answers
import mmap.database.answers.AnswersDTO
import mmap.database.answersstates.AnswerStates
import mmap.database.answerssnapshot.AnswersSnapshot
import java.util.*

fun updateAnswersStatement(
    answers: UpdateRowDTO<AnswersDTO, UUID>
) {
    Answers.batchInsertStatement(answers.insert)
    AnswersSnapshot.batchInsertSnapshotsStatement(answers.insert)
    AnswerStates.batchInsertCreateEventsStatement(answers.insert)

    AnswersSnapshot.updateSnapshotStatement(answers.update)
    AnswerStates.batchInsertUpdateEventsStatement(answers.update)

    AnswersSnapshot.removeSnapshotStatement(answers.remove)
    AnswerStates.batchInsertRemoveEventsStatement(answers.remove)
}
