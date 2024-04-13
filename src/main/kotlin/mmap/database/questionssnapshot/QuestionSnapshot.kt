package mmap.database.questionssnapshot

import mmap.database.extensions.PGEnum
import mmap.database.questions.QuestionType
import mmap.database.questions.Questions
import mmap.database.questions.QuestionsDTO
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.update
import java.util.*

object QuestionSnapshot : Table() {

    val questionId = uuid("question_id").references(Questions.id)
    val text = text("text")
    val type = customEnumeration("type", "QuestionType",
        { value -> QuestionType.valueOf(value as String) },
        { PGEnum("QuestionType", it) }
    )
    val isDeleted = bool("is_deleted").default(false)

    fun batchInsertSnapshotsStatement(insert: List<QuestionsDTO>) {
        batchInsert(insert) { question ->
            this[questionId] = question.id
            this[text] = question.questionText
            this[QuestionSnapshot.type] = question.type
        }
    }

    fun updateSnapshotStatement(update: List<QuestionsDTO>) {
        update.map { question ->
            update({ questionId eq question.id }) {
                it[type] = question.type
                it[text] = question.questionText
            }
        }
    }

    fun removeSnapshotStatement(remove: List<UUID>) {
        remove.map {
            update({ questionId eq it }) {
                it[isDeleted] = true
            }
        }
    }
}
