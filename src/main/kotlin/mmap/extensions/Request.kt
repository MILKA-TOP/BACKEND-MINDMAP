package mmap.extensions

import io.ktor.http.*
import io.ktor.http.cio.*
import io.ktor.server.request.*

fun Request.sessionId(): String? {
    val authorizationHeader = headers["Authorization"]

    return when {
        authorizationHeader == null -> null
        authorizationHeader.contains("Basic") -> null
        else -> authorizationHeader.toString().removePrefix("Bearer").trim()
    }
}

val ApplicationRequest.deviceId: String get() = headers[HttpHeaders.DeviceId].orEmpty().trim()

val HttpHeaders.DeviceId: String get() = "X-Device-Id"
