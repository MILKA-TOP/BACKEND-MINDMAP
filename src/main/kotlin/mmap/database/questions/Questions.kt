package mmap.database.questions

import mmap.database.tests.Tests
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.batchInsert

object Questions : UUIDTable(columnName = "question_id") {
    val testId = uuid("test_id").references(Tests.id)

    fun batchInsertStatement(questions: List<QuestionsDTO>) {
        batchInsert(questions) { question ->
            this[id] = question.id
            this[testId] = question.testId
        }
    }

}