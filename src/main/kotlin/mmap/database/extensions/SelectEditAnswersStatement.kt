package mmap.database.extensions

import mmap.database.answers.Answers
import mmap.database.answers.AnswersDTO
import mmap.database.answersstates.AnswerActualState
import mmap.database.answersstates.AnswerStateJson
import mmap.database.answersstates.AnswerStateType
import mmap.database.nodes.Nodes
import mmap.database.questions.Questions
import mmap.database.questionsevents.QuestionActualState
import mmap.database.questionsevents.QuestionStateType
import mmap.database.tests.Tests
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.booleanParam
import org.jetbrains.exposed.sql.selectAll

fun selectEditAnswersStatement(mapId: Int): List<AnswersDTO> =
    (Nodes innerJoin Tests innerJoin Questions innerJoin QuestionActualState innerJoin Answers innerJoin AnswerActualState)
        .selectAll()
        .where {
            (Nodes.mapId eq mapId) and
                    (Nodes.isRemoved eq booleanParam(false)) and
                    (QuestionActualState.stateType neq QuestionStateType.REMOVE) and
                    (AnswerActualState.stateType neq AnswerStateType.REMOVE)
        }
        .map {
            val answerEventData = when (val event = it[AnswerActualState.stateData]) {
                is AnswerStateJson.Insert -> AnswerEventJsonModel(event.text, event.isCorrect)
                is AnswerStateJson.Update -> AnswerEventJsonModel(event.text, event.isCorrect)
                AnswerStateJson.Remove -> throw IllegalArgumentException("Answer remove")
            }

            AnswersDTO(
                id = it[Answers.id].value,
                questionId = it[Answers.questionId],
                answerText = answerEventData.text,
                isCorrect = answerEventData.isCorrect,
                isRemoved = false
            )
        }
