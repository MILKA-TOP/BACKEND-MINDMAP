package mmap.data.tests.yandex

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import mmap.core.IgnoreCoverage
import mmap.data.tests.yandex.models.Yandex300Request
import mmap.domain.tests.models.Yandex300Response

@IgnoreCoverage
class Yandex300DataSource(
    private val client: HttpClient,
) {

    suspend fun getSummarization(link: String): Yandex300Response = client.post(
        urlString = "https://300.ya.ru/api/sharing-url",
    ) {
        contentType(ContentType.Application.Json)
        setBody(Yandex300Request(link))
    }.body<Yandex300Response>()

    suspend fun getPage(link: String) = client.get(
        urlString = link,
    ).body<String>()
}
