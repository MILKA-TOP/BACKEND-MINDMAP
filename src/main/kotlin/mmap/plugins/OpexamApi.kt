package mmap.plugins

import io.ktor.client.*
import io.ktor.client.engine.apache5.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*

var opexamsClient: HttpClient? = null

fun configureOpexamsApi(apiKey: String) {
    opexamsClient = HttpClient(Apache5) {

        expectSuccess = true
        defaultRequest {
            headers {
                append("api-key", apiKey)
            }
        }
        install(ContentNegotiation) { json(provideJson()) }
        install(HttpTimeout)
    }
}
