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

enum class QuestionEventType { CREATE, UPDATE, REMOVE }

object QuestionEvents : UUIDTable(columnName = "event_id") {
    val questionId = uuid("question_id").references(Questions.id)
    val eventType =
        defaultCustomEnumeration("event_type", "QuestionEventType") { QuestionEventType.valueOf(it as String) }
    val eventData = jsonb<QuestionEventJson>("event_data", JSONB_FORMAT)
    val createdAt = timestamp("created_at")

    fun batchInsertCreateEventsStatement(events: List<QuestionsDTO>) {
        batchInsert(events) { question ->
            this[questionId] = question.id
            this[eventType] = QuestionEventType.CREATE
            this[eventData] = QuestionEventJson.Insert(
                text = question.questionText,
                type = question.type,
            )
            this[createdAt] = Instant.now()
        }
    }

    fun batchInsertUpdateEventsStatement(update: List<QuestionsDTO>) {
        batchInsert(update) { question ->
            this[questionId] = question.id
            this[eventType] = QuestionEventType.UPDATE
            this[eventData] = QuestionEventJson.Update(
                text = question.questionText,
                type = question.type,
            )
            this[createdAt] = Instant.now()
        }
    }

    fun batchInsertRemoveEventsStatement(remove: List<UUID>) {
        batchInsert(remove) { id ->
            this[questionId] = id
            this[eventType] = QuestionEventType.REMOVE
            this[eventData] = QuestionEventJson.Remove
            this[createdAt] = Instant.now()
        }
    }
}