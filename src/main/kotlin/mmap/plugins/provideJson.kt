package mmap.plugins

import kotlinx.serialization.json.Json

internal fun provideJson() = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }
