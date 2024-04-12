package mmap.database.extensions

import mmap.database.answerprogress.AnswerProgress
import mmap.database.answers.Answers
import mmap.database.mapfetchtime.MapFetchTime
import mmap.database.questions.Questions
import mmap.database.tests.Tests
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

fun selectFetchIdForTest(testId: UUID, userId: Int): UUID? = transaction {
    (MapFetchTime innerJoin AnswerProgress innerJoin Answers innerJoin Questions innerJoin Tests)
        .selectAll()
        .where { (Tests.id eq testId) and (MapFetchTime.userId eq userId) }
        .firstOrNull()?.let { it[MapFetchTime.id].value }
}
