package mmap.database.answerssnapshot

import mmap.database.answers.Answers
import mmap.database.answers.AnswersDTO
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.update
import java.util.*

object AnswersSnapshot : Table() {
    val answerId = uuid("answer_id").references(Answers.id)
    val text = text("text")
    val isCorrect = bool("is_correct")
    val isDeleted = bool("is_deleted").default(false)

    fun batchInsertSnapshotsStatement(insert: List<AnswersDTO>) {
        batchInsert(insert) { answer ->
            this[answerId] = answer.id
            this[text] = answer.answerText
            this[isCorrect] = answer.isCorrect
        }
    }

    fun updateSnapshotStatement(update: List<AnswersDTO>) {
        update.map { answer ->
            update({ answerId eq answer.id }) {
                it[text] = answer.answerText
                it[isCorrect] = answer.isCorrect
            }
        }
    }

    fun removeSnapshotStatement(remove: List<UUID>) {
        remove.map {
            update({ answerId eq it }) {
                it[isDeleted] = true
            }
        }
    }
}