package mmap.database.answersevents

import mmap.database.answers.Answers
import mmap.database.answers.AnswersDTO
import mmap.database.extensions.defaultCustomEnumeration
import mmap.extensions.JSONB_FORMAT
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.json.jsonb
import java.time.Instant
import java.util.*

enum class AnswerEventType { CREATE, UPDATE, REMOVE }

object AnswerEvents : UUIDTable(columnName = "event_id") {
    val answerId = uuid("answer_id").references(Answers.id)
    val eventType = defaultCustomEnumeration("event_type", "AnswerEventType") { AnswerEventType.valueOf(it as String) }
    val eventData = jsonb<AnswerEventJson>("event_data", JSONB_FORMAT)
    val createdAt = timestamp("created_at")

    fun batchInsertCreateEventsStatement(events: List<AnswersDTO>) {
        batchInsert(events) { answer ->
            this[answerId] = answer.id
            this[eventType] = AnswerEventType.CREATE
            this[eventData] = AnswerEventJson.Insert(
                text = answer.answerText,
                isCorrect = answer.isCorrect,
            )
            this[createdAt] = Instant.now()
        }
    }

    fun batchInsertUpdateEventsStatement(update: List<AnswersDTO>) {
        batchInsert(update) { answer ->
            this[answerId] = answer.id
            this[eventType] = AnswerEventType.UPDATE
            this[eventData] = AnswerEventJson.Update(
                text = answer.answerText,
                isCorrect = answer.isCorrect,
            )
            this[createdAt] = Instant.now()
        }
    }

    fun batchInsertRemoveEventsStatement(remove: List<UUID>) {
        batchInsert(remove) { id ->
            this[answerId] = id
            this[eventType] = AnswerEventType.REMOVE
            this[eventData] = AnswerEventJson.Remove
            this[createdAt] = Instant.now()
        }
    }
}
