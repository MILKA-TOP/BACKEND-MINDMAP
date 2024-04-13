package mmap.database.extensions

import mmap.database.mapfetchtime.MapFetchTime
import mmap.database.maps.Maps
import mmap.database.nodes.Nodes
import mmap.database.questions.QuestionType
import mmap.database.questions.Questions
import mmap.database.questions.QuestionsDTO
import mmap.database.questionsevents.QuestionEventJson
import mmap.database.questionsevents.QuestionEvents
import mmap.database.tests.Tests
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.booleanParam
import org.jetbrains.exposed.sql.selectAll

fun selectMapsStampedQuestionsStatement(
    mapId: Int,
): List<QuestionsDTO> = (MapFetchTime
    .innerJoin(Maps)
    .innerJoin(Nodes)
    .innerJoin(Tests)
    .innerJoin(Questions)
    .innerJoin(QuestionEvents)
        ).selectAll().where {
                (Nodes.mapId eq mapId) and
                (Nodes.isRemoved eq booleanParam(false)) and
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

internal data class QuestionEventJsonModel(
    val text: String,
    val type: QuestionType
)
