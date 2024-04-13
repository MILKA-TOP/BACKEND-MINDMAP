package mmap.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import mmap.features.user.POST_REQUEST_CREATE_TOKEN
import mmap.features.user.UserController

fun Application.configureAuthentication() {
    install(Authentication) {
        bearer("auth-bearer") {
//            realm = "Access to the '/' path"
            authenticate { tokenCredential ->
                val userId = when {
                    this.request.uri.contains(POST_REQUEST_CREATE_TOKEN) ->
                        UserController.getTemporaryUserIdByToken(tokenCredential)

                    else -> UserController.getActiveUserIdByToken(tokenCredential)
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
