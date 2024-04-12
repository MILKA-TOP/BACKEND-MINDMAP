package mmap.extensions

import kotlinx.serialization.json.Json

val JSONB_FORMAT = Json {
    prettyPrint = true
    useArrayPolymorphism = true
}
