package mmap.database.extensions

import mmap.database.answerprogress.AnswerProgress
import mmap.database.answers.Answers
import mmap.database.mapfetchtime.MapFetchTime
import mmap.database.nodes.Nodes
import mmap.database.questions.Questions
import mmap.database.tests.Tests
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.booleanParam
import org.jetbrains.exposed.sql.selectAll
import java.util.*

fun selectPickedAnswersIdsStatement(
    mapId: Int,
    userId: Int,
): List<UUID> = (MapFetchTime
    .innerJoin(AnswerProgress)
    .innerJoin(Answers)
    .innerJoin(Questions)
    .innerJoin(Tests)
    .innerJoin(Nodes)
        ).selectAll().where {
        (AnswerProgress.userId eq userId) and
                (Nodes.mapId eq mapId) and
                (AnswerProgress.isRemoved eq booleanParam(false)) and
                (Nodes.isRemoved eq booleanParam(false))
    }.map {
        it[AnswerProgress.answerId]
    }
