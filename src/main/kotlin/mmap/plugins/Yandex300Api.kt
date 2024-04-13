package mmap.plugins

import io.ktor.client.*
import io.ktor.client.engine.apache5.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import mmap.di.appModule
import org.koin.core.qualifier.StringQualifier

val YANDEX_300_CLIENT_QUALIFIER = StringQualifier("YANDEX_300_CLIENT_QUALIFIER")

fun configureYandex300Api(apiKey: String) {
    appModule.apply {
        single(YANDEX_300_CLIENT_QUALIFIER) {
            HttpClient(Apache5) {
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
    }
}
