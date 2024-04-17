package mmap.database.answersstates

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

enum class AnswerStateType { CREATE, UPDATE, REMOVE }

object AnswerStates : UUIDTable(columnName = "state_id") {
    val answerId = uuid("answer_id").references(Answers.id)
    val stateType = defaultCustomEnumeration("state_type", "AnswerStateType") { AnswerStateType.valueOf(it as String) }
    val stateData = jsonb<AnswerStateJson>("state_data", JSONB_FORMAT)
    val createdAt = timestamp("created_at")

    fun batchInsertCreateEventsStatement(events: List<AnswersDTO>) {
        batchInsert(events) { answer ->
            this[answerId] = answer.id
            this[stateType] = AnswerStateType.CREATE
            this[stateData] = AnswerStateJson.Insert(
                text = answer.answerText,
                isCorrect = answer.isCorrect,
            )
            this[createdAt] = Instant.now()
        }
    }

    fun batchInsertUpdateEventsStatement(update: List<AnswersDTO>) {
        batchInsert(update) { answer ->
            this[answerId] = answer.id
            this[stateType] = AnswerStateType.UPDATE
            this[stateData] = AnswerStateJson.Update(
                text = answer.answerText,
                isCorrect = answer.isCorrect,
            )
            this[createdAt] = Instant.now()
        }
    }

    fun batchInsertRemoveEventsStatement(remove: List<UUID>) {
        batchInsert(remove) { id ->
            this[answerId] = id
            this[stateType] = AnswerStateType.REMOVE
            this[stateData] = AnswerStateJson.Remove
            this[createdAt] = Instant.now()
        }
    }
}
