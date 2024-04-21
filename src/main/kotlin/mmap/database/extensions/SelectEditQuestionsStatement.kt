package mmap.database.extensions

import mmap.database.nodes.Nodes
import mmap.database.questions.Questions
import mmap.database.questions.QuestionsDTO
import mmap.database.questionsevents.QuestionActualState
import mmap.database.questionsevents.QuestionStateType
import mmap.database.questionsevents.QuestionsStateJson
import mmap.database.tests.Tests
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.booleanParam
import org.jetbrains.exposed.sql.selectAll

fun selectEditQuestionsStatement(mapId: Int): List<QuestionsDTO> =
    (Nodes innerJoin Tests innerJoin Questions innerJoin QuestionActualState)
        .selectAll()
        .where {
            (Nodes.mapId eq mapId) and
                    (Nodes.isRemoved eq booleanParam(false)) and
                    (QuestionActualState.stateType neq QuestionStateType.REMOVE)
        }
        .map {
            val questionEventData = when (val event = it[QuestionActualState.stateData]) {
                is QuestionsStateJson.Insert -> QuestionEventJsonModel(event.text, event.type)
                is QuestionsStateJson.Update -> QuestionEventJsonModel(event.text, event.type)
                QuestionsStateJson.Remove -> throw IllegalArgumentException("Question remove")
            }
            QuestionsDTO(
                id = it[Questions.id].value,
                testId = it[Questions.testId],
                questionText = questionEventData.text,
                type = questionEventData.type,
                isDeleted = false
            )
        }
