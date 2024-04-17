package mmap.database.questionsevents

import mmap.database.extensions.defaultCustomEnumeration
import mmap.database.questions.Questions
import mmap.database.questions.QuestionsDTO
import mmap.extensions.JSONB_FORMAT
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.json.jsonb
import java.time.Instant
import java.util.*

enum class QuestionStateType { CREATE, UPDATE, REMOVE }

object QuestionStates : UUIDTable(columnName = "state_id") {
    val questionId = uuid("question_id").references(Questions.id)
    val stateType =
        defaultCustomEnumeration("state_type", "QuestionStateType") { QuestionStateType.valueOf(it as String) }
    val stateData = jsonb<QuestionsStateJson>("state_data", JSONB_FORMAT)
    val createdAt = timestamp("created_at")

    fun batchInsertCreateEventsStatement(events: List<QuestionsDTO>) {
        batchInsert(events) { question ->
            this[questionId] = question.id
            this[stateType] = QuestionStateType.CREATE
            this[stateData] = QuestionsStateJson.Insert(
                text = question.questionText,
                type = question.type,
            )
            this[createdAt] = Instant.now()
        }
    }

    fun batchInsertUpdateEventsStatement(update: List<QuestionsDTO>) {
        batchInsert(update) { question ->
            this[questionId] = question.id
            this[stateType] = QuestionStateType.UPDATE
            this[stateData] = QuestionsStateJson.Update(
                text = question.questionText,
                type = question.type,
            )
            this[createdAt] = Instant.now()
        }
    }

    fun batchInsertRemoveEventsStatement(remove: List<UUID>) {
        batchInsert(remove) { id ->
            this[questionId] = id
            this[stateType] = QuestionStateType.REMOVE
            this[stateData] = QuestionsStateJson.Remove
            this[createdAt] = Instant.now()
        }
    }
}
