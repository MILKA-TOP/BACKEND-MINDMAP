package mmap.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import mmap.features.auth.login.POST_REQUEST_CREATE_TOKEN
import mmap.features.auth.sessions.SessionController

fun Application.configureAuthentication() {
    install(Authentication) {
        bearer("auth-bearer") {
//            realm = "Access to the '/' path"
            authenticate { tokenCredential ->
                val userId = when {
                    this.request.uri.contains(POST_REQUEST_CREATE_TOKEN) ->
                        SessionController.getTemporaryUserIdByToken(tokenCredential)

                    else -> SessionController.getActiveUserIdByToken(tokenCredential)
                }
                if (userId != null) {
                    UserIdPrincipal(userId.toString())
                } else {
                    null
                }
            }
        }
    }
}

fun Application.authenticateRouting(build: Route.() -> Unit) = routing {
    authenticate("auth-bearer") {
        build()
    }
}

private val TemporaryBearerRequests: List<String> = listOf(
    POST_REQUEST_CREATE_TOKEN,
)