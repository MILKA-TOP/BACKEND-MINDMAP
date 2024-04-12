package mmap.database.extensions

import mmap.database.answerprogress.AnswerProgress
import mmap.database.answers.Answers
import mmap.database.maps.Maps
import mmap.database.nodes.Nodes
import mmap.database.questions.Questions
import mmap.database.tests.Tests
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update

fun updateAnswerProgressUnmarkStatement(userId: Int, mapId: Int) =
    (Maps innerJoin Nodes
            innerJoin Tests
            innerJoin Questions
            innerJoin Answers
            innerJoin AnswerProgress)
        .update({
            (Maps.id eq mapId) and (AnswerProgress.userId eq userId)
        }) {
            it[AnswerProgress.isRemoved] = true
        }
