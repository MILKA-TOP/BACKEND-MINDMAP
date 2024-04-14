package mmap.data.tests.opexams

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import mmap.domain.tests.models.opexams.*

class OpexamsDataSource(
    private val client: HttpClient,
) {

    suspend fun getSummarization(description: String): List<OpexamsQuestion> = client.post(
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
    }.body<OpexamsQuestionData>().data
}
