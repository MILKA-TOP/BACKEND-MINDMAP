package mmap.features.testing.yandex

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mmap.plugins.yandex300Client
import org.jsoup.Jsoup

class YandexRepository {

    suspend fun getLinkDescription(link: String): String? {
        val summaryLink = kotlin.runCatching {
            yandex300Client?.post(
                urlString = "https://300.ya.ru/api/sharing-url",
            ) {
                contentType(ContentType.Application.Json)
                setBody(Yandex300Request(link))
            }?.body<Yandex300Response>()
        }.getOrNull()

        return (summaryLink as? Yandex300Response.Success)?.sharing_url?.let {
            kotlin.runCatching {
                yandex300Client?.get(
                    urlString = it,
                )?.body<String>()
            }.getOrNull()?.let {
                Jsoup.parse(it).getElementsByTag("meta").first { it.attr("name") == "description" }.attr("content")
            }
        }
    }

    @Serializable
    @Suppress("ConstructorParameterNaming")
    private class Yandex300Request(
        val article_url: String,
    )

    @Serializable(with = Yandex300ResponseSerializer::class)
    @Suppress("ConstructorParameterNaming")
    sealed class Yandex300Response {
        open val status: YandexResponseStatus = YandexResponseStatus.error

        @Serializable
        data class Success(
            override val status: YandexResponseStatus = YandexResponseStatus.success,
            val sharing_url: String,
        ) : Yandex300Response()

        @Serializable
        data class Error(
            override val status: YandexResponseStatus = YandexResponseStatus.error,
            val message: String,
        ) : Yandex300Response()
    }

    enum class YandexResponseStatus {
        error, success
    }

    internal object Yandex300ResponseSerializer :
        JsonContentPolymorphicSerializer<Yandex300Response>(Yandex300Response::class),
        KSerializer<Yandex300Response> {

        override fun selectDeserializer(element: JsonElement): KSerializer<out Yandex300Response> {
            val typeString = element.jsonObject["status"]
                ?.jsonPrimitive
                ?.takeIf { it.isString }
                ?.content.orEmpty()
            return when (YandexResponseStatus.valueOf(typeString)) {
                YandexResponseStatus.success -> Yandex300Response.Success.serializer()
                YandexResponseStatus.error -> Yandex300Response.Error.serializer()
            }
        }
    }
}
