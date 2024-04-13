package mmap.features.testing.tests

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import mmap.database.answers.AnswersDTO
import mmap.database.questions.QuestionType
import mmap.database.questions.QuestionsDTO
import mmap.features.testing.tests.models.QuestionGeneratedModel
import mmap.plugins.opexamsClient
import java.util.*

class OpexamsRepository {

    suspend fun generate(testId: UUID, description: String): QuestionGeneratedModel {

        val questions = opexamsClient?.post(
            urlString = "https://api.opexams.com/questions-generator"
        ) {
            contentType(ContentType.Application.Json)
            setBody(
                OpexamsRequestModel(
                    type = OpexamsRequestType.contextBased,
                    questionType = OpexamsQuestionsType.MCQ,
                    context = description
                )
            )
        }?.body<OpexamsQuestionData>()?.data ?: emptyList()

        val questionsDto = mutableListOf<QuestionsDTO>()
        val answersDto = mutableListOf<AnswersDTO>()

        questions.forEach { question ->
            val questionId = UUID.randomUUID()

            questionsDto.add(
                QuestionsDTO(
                    id = questionId,
                    testId = testId,
                    questionText = question.question,
                    type = QuestionType.SINGLE_CHOICE
                )
            )
            question.options.map { answer ->
                answersDto.add(
                    AnswersDTO(
                        id = UUID.randomUUID(),
                        questionId = questionId,
                        answerText = answer,
                        isCorrect = answer == question.answer
                    )
                )
            }
        }

        return QuestionGeneratedModel(
            questions = questionsDto,
            answers = answersDto,
        )
    }

    @Suppress("EnumNaming", "EnumEntryNameCase")
    enum class OpexamsRequestType {
        contextBased, topicBased
    }

    enum class OpexamsQuestionsType {
        MCQ
    }

    @Serializable
    private class OpexamsRequestModel(
        val type: OpexamsRequestType,
        val questionType: OpexamsQuestionsType,
        val context: String,
    )

    @Serializable
    private data class OpexamsQuestionData(
        val data: List<OpexamsQuestion>
    )

    @Serializable
    private data class OpexamsQuestion(
        val id: String,
        val question: String,
        val answer: String,
        val options: List<String>
    )
}
