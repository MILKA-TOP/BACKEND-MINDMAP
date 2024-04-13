package mmap.plugins

import io.ktor.client.*
import io.ktor.client.engine.apache5.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*

var yandex300Client: HttpClient? = null

fun configureYandex300Api(apiKey: String) {
    yandex300Client = HttpClient(Apache5) {
        expectSuccess = true
        defaultRequest {
            headers {
                append("Authorization", "OAuth $apiKey")
            }
        }
        install(ContentNegotiation) { json(provideJson()) }
        install(HttpTimeout)
    }
}
