package mmap.data.tests.yandex.models

import kotlinx.serialization.Serializable

@Serializable
@Suppress("ConstructorParameterNaming")
class Yandex300Request(
    val article_url: String,
)
