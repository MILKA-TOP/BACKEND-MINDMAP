package mmap.features.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mmap.core.ApiResponse.Companion.respond
import mmap.domain.auth.models.request.EnterDataReceiveRemote
import mmap.domain.auth.models.request.LoginRequestRemote
import mmap.domain.auth.models.request.RegistryReceiveRemote
import mmap.domain.auth.models.response.CreateTokenRemoteResponse
import mmap.extensions.deviceId
import mmap.plugins.authenticateRouting
import org.koin.ktor.ext.inject

fun Application.configureUserRouting() {

    val userController by inject<UserController>()

    routing {
        post("/user/enter-auth-data") {
            val deviceId = call.request.deviceId
            val enterData = call.receive<EnterDataReceiveRemote>()
            val loginResponse = userController.performLogin(enterData, deviceId)
            loginResponse.respond(call)
        }
    }

    authenticateRouting {
        post(POST_REQUEST_CREATE_TOKEN) {
            val userId = call.principal<UserIdPrincipal>()?.name!!.toInt()
            val deviceId = call.request.deviceId

            val token = userController.generateToken(userId, deviceId)
            userController.connectUser(userId, deviceId, token)

            call.respond(HttpStatusCode.OK, CreateTokenRemoteResponse(token))
        }
    }

    routing {
        post("/user/login") {
            val deviceId = call.request.deviceId
            val loginRequestData = call.receive<LoginRequestRemote>()
            val userId = loginRequestData.userId.toInt()

            val session = userController.tryConnectUser(userId, deviceId, loginRequestData.pinToken)
            call.respond(HttpStatusCode.OK, session)
        }
    }

    routing {
        post("/user/revoke-device{userId}") {
            val deviceId = call.request.deviceId
            val userId = call.parameters["userId"]!!.toInt()

            userController.tryRevokeDevice(userId, deviceId)
            call.respond(HttpStatusCode.OK)
        }
    }

    routing {
        post("/user/registry") {
            val deviceId = call.request.deviceId
            val registryData = call.receive<RegistryReceiveRemote>()
            val sessionResponse = userController.performRegistry(registryData, deviceId)
            call.respond(HttpStatusCode.OK, sessionResponse)
        }
    }
}

const val POST_REQUEST_CREATE_TOKEN = "/user/create-token"
