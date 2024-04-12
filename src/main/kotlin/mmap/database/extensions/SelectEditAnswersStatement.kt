package mmap.database.extensions

import mmap.database.answers.Answers
import mmap.database.answers.AnswersDTO
import mmap.database.answerssnapshot.AnswersSnapshot
import mmap.database.nodes.Nodes
import mmap.database.questions.Questions
import mmap.database.questionssnapshot.QuestionSnapshot
import mmap.database.tests.Tests
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.booleanParam
import org.jetbrains.exposed.sql.selectAll

fun selectEditAnswersStatement(mapId: Int): List<AnswersDTO> =
    (Nodes innerJoin Tests innerJoin Questions innerJoin QuestionSnapshot innerJoin Answers innerJoin AnswersSnapshot)
        .selectAll()
        .where {
            (Nodes.mapId eq mapId) and
                    (Nodes.isRemoved eq booleanParam(false)) and
                    (QuestionSnapshot.isDeleted eq booleanParam(false)) and
                    (AnswersSnapshot.isDeleted eq booleanParam(false))
        }
        .map {
            AnswersDTO(
                id = it[Answers.id].value,
                questionId = it[Answers.questionId],
                answerText = it[AnswersSnapshot.text],
                isCorrect = it[AnswersSnapshot.isCorrect],
                isRemoved = it[AnswersSnapshot.isDeleted]
            )
        }
