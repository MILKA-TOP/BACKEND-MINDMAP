package mmap.database.extensions

import mmap.database.answers.Answers
import mmap.database.answers.AnswersDTO
import mmap.database.answersstates.AnswerStateJson
import mmap.database.answersstates.AnswerStates
import mmap.database.mapfetchtime.MapFetchTime
import mmap.database.maps.Maps
import mmap.database.nodes.Nodes
import mmap.database.questions.Questions
import mmap.database.tests.Tests
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.booleanParam
import org.jetbrains.exposed.sql.selectAll

fun selectMapsStampedAnswersStatement(
    mapId: Int,
): List<AnswersDTO> = (MapFetchTime
    .innerJoin(Maps)
    .innerJoin(Nodes)
    .innerJoin(Tests)
    .innerJoin(Questions)
    .innerJoin(Answers)
    .innerJoin(AnswerStates)
        ).selectAll().where {
        (Nodes.mapId eq mapId) and
                (Nodes.isRemoved eq booleanParam(false)) and
                (AnswerStates.createdAt less MapFetchTime.fetchedAt)
    }
    .orderBy(AnswerStates.createdAt to SortOrder.ASC)
    .groupBy { it[AnswerStates.answerId] }
    .map { (_, rows) ->
        val lastEvent = rows.last()
        val answerEventData = when (val event = lastEvent[AnswerStates.stateData]) {
            AnswerStateJson.Remove -> null
            is AnswerStateJson.Insert -> AnswerEventJsonModel(event.text, event.isCorrect)
            is AnswerStateJson.Update -> AnswerEventJsonModel(event.text, event.isCorrect)
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

internal data class AnswerEventJsonModel(
    val text: String,
    val isCorrect: Boolean
)
