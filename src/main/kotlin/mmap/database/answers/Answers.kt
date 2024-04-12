package mmap.database.answers

import mmap.database.questions.Questions
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.batchInsert

object Answers : UUIDTable(columnName = "answer_id") {
    val questionId = uuid("question_id").references(Questions.id)

    fun batchInsertStatement(questions: List<AnswersDTO>) {
        batchInsert(questions) { answers ->
            this[id] = answers.id
            this[questionId] = answers.questionId
        }
    }
}