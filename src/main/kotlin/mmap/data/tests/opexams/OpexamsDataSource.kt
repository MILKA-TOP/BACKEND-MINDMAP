package mmap.data.tests.opexams

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import mmap.domain.tests.models.opexams.OpexamsQuestionData
import mmap.domain.tests.models.opexams.OpexamsQuestionsType
import mmap.domain.tests.models.opexams.OpexamsRequestModel
import mmap.domain.tests.models.opexams.OpexamsRequestType

class OpexamsDataSource(
    private val client: HttpClient,
) {

    suspend fun getSummarization(description: String) = client.post(
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
