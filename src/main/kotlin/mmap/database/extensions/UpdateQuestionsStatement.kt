package mmap.database.extensions

import mmap.database.questions.Questions
import mmap.database.questions.QuestionsDTO
import mmap.database.questionsevents.QuestionStates
import mmap.database.questionssnapshot.QuestionSnapshot
import java.util.*

fun updateQuestionsStatement(
    questions: UpdateRowDTO<QuestionsDTO, UUID>
) {
    Questions.batchInsertStatement(questions.insert)
    QuestionSnapshot.batchInsertSnapshotsStatement(questions.insert)
    QuestionStates.batchInsertCreateEventsStatement(questions.insert)

    QuestionSnapshot.updateSnapshotStatement(questions.update)
    QuestionStates.batchInsertUpdateEventsStatement(questions.update)

    QuestionSnapshot.removeSnapshotStatement(questions.remove)
    QuestionStates.batchInsertRemoveEventsStatement(questions.remove)
}
