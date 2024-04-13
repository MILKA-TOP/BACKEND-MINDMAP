package mmap.database.answerprogress

import mmap.database.answers.Answers
import mmap.database.mapfetchtime.MapFetchTime
import mmap.database.nodes.Nodes.default
import mmap.database.users.Users
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object AnswerProgress : Table() {
    val userId = integer("user_id").references(Users.id)
    val answerId = uuid("answer_id").references(Answers.id)
    val fetchId = uuid("fetch_id").references(MapFetchTime.id)
    val isRemoved = bool("is_removed").default(false)

    fun addAnswers(answers: List<AnswersProgressDTO>) = transaction {
        batchInsert(answers) { answer ->
            this[userId] = answer.userId
            this[answerId] = answer.answerId
            this[fetchId] = answer.fetchId
        }
    }
}
