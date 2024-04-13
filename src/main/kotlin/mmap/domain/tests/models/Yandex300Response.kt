package mmap.domain.tests.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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

@Suppress("EnumNaming")
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
