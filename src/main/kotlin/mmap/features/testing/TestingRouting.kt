package mmap.features.testing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mmap.core.ApiResponse.Companion.respond
import mmap.domain.tests.models.TestingCompleteRequestRemote
import mmap.plugins.authenticateRouting
import org.koin.ktor.ext.inject
import java.util.*

fun Application.configureTestingRouting() {

    val testingController by inject<TestingController>()
    authenticateRouting {
        post("/tests/submit-test{testId}") {
            val userId = call.principal<UserIdPrincipal>()?.name!!.toInt()
            val nodeId = UUID.fromString(call.parameters["testId"]!!)
            val usersTestAnswers = call.receive<TestingCompleteRequestRemote>()
            val response = testingController.completeTest(userId, nodeId, usersTestAnswers)
            response.respond(call)
        }
    }
    authenticateRouting {
        post("/tests/generate{nodeId}") {
            val userId = call.principal<UserIdPrincipal>()?.name!!.toInt()
            val nodeId = UUID.fromString(call.parameters["nodeId"]!!)
            val testResult = testingController.generateTest(userId, nodeId)
            call.respond(HttpStatusCode.OK, testResult)
        }
    }
}
