package mmap.core

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

class ApiResponse<T>(
    val statusCode: HttpStatusCode = HttpStatusCode.OK,
    val data: T? = null,
    val errorMessage: String? = null
) {

    companion object {
        suspend inline fun <reified T> ApiResponse<T>.respond(call: ApplicationCall) {
            when {
                data == null && errorMessage == null -> call.respond(statusCode)
                data != null && errorMessage == null -> call.respond(statusCode, data)
            }
            call.respond(statusCode)
        }
    }
}
