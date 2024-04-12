package mmap.database.extensions

import mmap.database.questions.Questions
import mmap.database.questions.QuestionsDTO
import mmap.database.questionsevents.QuestionEvents
import mmap.database.questionssnapshot.QuestionSnapshot
import java.util.*

fun updateQuestionsStatement(
    questions: UpdateRowDTO<QuestionsDTO, UUID>
) {
    Questions.batchInsertStatement(questions.insert)
    QuestionSnapshot.batchInsertSnapshotsStatement(questions.insert)
    QuestionEvents.batchInsertCreateEventsStatement(questions.insert)

    QuestionSnapshot.updateSnapshotStatement(questions.update)
    QuestionEvents.batchInsertUpdateEventsStatement(questions.update)

    QuestionSnapshot.removeSnapshotStatement(questions.remove)
    QuestionEvents.batchInsertRemoveEventsStatement(questions.remove)
}