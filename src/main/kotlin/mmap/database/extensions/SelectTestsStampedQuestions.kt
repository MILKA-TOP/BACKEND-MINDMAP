package mmap.database.extensions

import mmap.database.answerprogress.AnswerProgress
import mmap.database.mapfetchtime.MapFetchTime
import mmap.database.maps.Maps
import mmap.database.nodes.Nodes
import mmap.database.questions.Questions
import mmap.database.questions.QuestionsDTO
import mmap.database.questionsevents.QuestionEventJson
import mmap.database.questionsevents.QuestionEvents
import mmap.database.tests.Tests
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.booleanParam
import org.jetbrains.exposed.sql.selectAll
import java.util.*

fun selectTestsStampedQuestions(
    testId: UUID,
    fetchId: UUID,
): List<QuestionsDTO> = (MapFetchTime
    .innerJoin(Maps)
    .innerJoin(Nodes)
    .innerJoin(Tests)
    .innerJoin(Questions)
    .innerJoin(QuestionEvents)
        ).selectAll().where {
        (MapFetchTime.id eq fetchId) and
                (Tests.id eq testId) and
                (QuestionEvents.createdAt less MapFetchTime.fetchedAt)
    }
    .orderBy(QuestionEvents.createdAt to SortOrder.ASC)
    .groupBy { it[QuestionEvents.questionId] }
    .map { (_, rows) ->
        val lastEvent = rows.last()
        val questionEventData = when (val event = lastEvent[QuestionEvents.eventData]) {
            QuestionEventJson.Remove -> null
            is QuestionEventJson.Insert -> QuestionEventJsonModel(event.text, event.type)
            is QuestionEventJson.Update -> QuestionEventJsonModel(event.text, event.type)
        }
        questionEventData?.let { data ->
            QuestionsDTO(
                id = lastEvent[Questions.id].value,
                testId = lastEvent[Questions.testId],
                questionText = data.text,
                type = data.type,
            )
        }
    }.filterNotNull()

