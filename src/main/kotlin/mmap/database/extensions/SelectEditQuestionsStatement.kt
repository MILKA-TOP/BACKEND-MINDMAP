package mmap.database.extensions

import mmap.database.nodes.Nodes
import mmap.database.questions.Questions
import mmap.database.questions.QuestionsDTO
import mmap.database.questionssnapshot.QuestionSnapshot
import mmap.database.tests.Tests
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.booleanParam
import org.jetbrains.exposed.sql.selectAll

fun selectEditQuestionsStatement(mapId: Int): List<QuestionsDTO> =
    (Nodes innerJoin Tests innerJoin Questions innerJoin QuestionSnapshot)
        .selectAll()
        .where {
            (Nodes.mapId eq mapId) and
                    (Nodes.isRemoved eq booleanParam(false)) and
                    (QuestionSnapshot.isDeleted eq booleanParam(false))
        }
        .map {
            QuestionsDTO(
                id = it[Questions.id].value,
                testId = it[Questions.testId],
                questionText = it[QuestionSnapshot.text],
                type = it[QuestionSnapshot.type],
                isDeleted = it[QuestionSnapshot.isDeleted]
            )
        }
