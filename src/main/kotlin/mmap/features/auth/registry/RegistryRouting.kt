package mmap.features.auth.registry

import io.ktor.server.application.*
import io.ktor.server.routing.*
import mmap.features.auth.sessions.SessionController

fun Application.configureRegistryRouting() {

    routing {
        post("/user/registry") {
            val registryController = RegistryController(call)
            val registeredUser = registryController.performRegistry()
            registeredUser?.let { user ->
                val sessionController = SessionController(call)
                sessionController.temporaryConnectUser(user)
            }
        }
    }
}