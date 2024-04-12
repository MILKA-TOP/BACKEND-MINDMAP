package mmap.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import mmap.extensions.DeviceId

val RequestHeaderFilterPlugin = createApplicationPlugin(name = "RequestHeaderFilterPlugin") {
    onCall { call ->
        if (!call.request.headers.contains(HttpHeaders.DeviceId)) {
            call.respond(HttpStatusCode.BadRequest, "Missing device header")
        }
    }
}

fun Application.configureFilter() {
    install(RequestHeaderFilterPlugin)
}

