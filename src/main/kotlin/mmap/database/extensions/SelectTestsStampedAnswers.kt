package mmap.database.extensions

import mmap.database.answers.Answers
import mmap.database.answers.AnswersDTO
import mmap.database.answersevents.AnswerEventJson
import mmap.database.answersevents.AnswerEvents
import mmap.database.mapfetchtime.MapFetchTime
import mmap.database.maps.Maps
import mmap.database.nodes.Nodes
import mmap.database.questions.Questions
import mmap.database.tests.Tests
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.util.*

fun selectTestsStampedAnswers(
    testId: UUID,
    fetchId: UUID,
): List<AnswersDTO> = (MapFetchTime
    .innerJoin(Maps)
    .innerJoin(Nodes)
    .innerJoin(Tests)
    .innerJoin(Questions)
    .innerJoin(Answers)
    .innerJoin(AnswerEvents)
        ).selectAll().where {
        (MapFetchTime.id eq fetchId) and
                (Tests.id eq testId) and
                (AnswerEvents.createdAt less MapFetchTime.fetchedAt)
    }
    .orderBy(AnswerEvents.createdAt to SortOrder.ASC)
    .groupBy { it[AnswerEvents.answerId] }
    .map { (_, rows) ->
        val lastEvent = rows.last()
        val answerEventData = when (val event = lastEvent[AnswerEvents.eventData]) {
            AnswerEventJson.Remove -> null
            is AnswerEventJson.Insert -> AnswerEventJsonModel(event.text, event.isCorrect)
            is AnswerEventJson.Update -> AnswerEventJsonModel(event.text, event.isCorrect)
        }
        answerEventData?.let { data ->
            AnswersDTO(
                id = lastEvent[Answers.id].value,
                questionId = lastEvent[Answers.questionId],
                answerText = data.text,
                isCorrect = data.isCorrect,
            )
        }
    }.filterNotNull()
