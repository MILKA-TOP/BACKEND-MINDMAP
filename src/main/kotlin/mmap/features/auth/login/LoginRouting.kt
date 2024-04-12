package mmap.features.auth.login

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mmap.database.sessions.Sessions
import mmap.extensions.deviceId
import mmap.features.auth.login.models.request.LoginRequestRemote
import mmap.features.auth.login.models.response.CreateTokenRemoteResponse
import mmap.features.auth.sessions.SessionController
import mmap.plugins.authenticateRouting

fun Application.configureLoginRouting() {

    routing {
        post("/user/enter-auth-data") {
            val loginController = LoginController(call)
            val userDTO = loginController.performLogin()

            userDTO?.let { user ->
                val sessionController = SessionController(call)
                sessionController.temporaryConnectUser(user)
            }
        }
    }

    authenticateRouting {
        post(POST_REQUEST_CREATE_TOKEN) {
            val userId = call.principal<UserIdPrincipal>()?.name!!.toInt()
            val deviceId = call.request.deviceId!!.trim()

            val loginController = LoginController(call)
            val token = loginController.generateToken(userId, deviceId)

            val sessionController = SessionController(call)
            sessionController.connectUser(userId, deviceId, token)

            call.respond(HttpStatusCode.OK, CreateTokenRemoteResponse(token))
        }
    }

    routing {
        post("/user/login") {
            val deviceId = call.request.deviceId!!.trim()
            val loginRequestData = call.receive<LoginRequestRemote>()
            val userId = loginRequestData.userId.toInt()

            val sessionController = SessionController(call)
            sessionController.tryConnectUser(userId, deviceId, loginRequestData.pinToken)
        }
    }

    routing {
        post("/user/revoke-device{userId}") {
            val deviceId = call.request.deviceId!!.trim()
            val userId = call.parameters["userId"]!!.toInt()

            val sessionController = SessionController(call)
            sessionController.tryRevokeDevice(userId, deviceId)
        }
    }

}

const val POST_REQUEST_CREATE_TOKEN = "/user/create-token"
