package mmap.features.testing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import mmap.features.testing.tests.OpexamsRepository
import mmap.features.testing.yandex.YandexRepository
import mmap.plugins.authenticateRouting
import java.util.UUID

val yandexRepository: YandexRepository = YandexRepository()
val opexamsRepository: OpexamsRepository = OpexamsRepository()

fun Application.configureTestingRouting() {
    authenticateRouting {
        post("/tests/submit-test{testId}") {
            val userId = call.principal<UserIdPrincipal>()?.name!!
            val nodeId = call.parameters["testId"]!!

            val testingController = TestingController(call)
            testingController.completeTest(userId, nodeId)
        }
    }
    authenticateRouting {
        post("/tests/generate{nodeId}") {
            val userId = call.principal<UserIdPrincipal>()?.name!!.toInt()
            val nodeId = UUID.fromString(call.parameters["nodeId"]!!)
            val testingController = TestingController(call)
            testingController.generateTest(userId, nodeId)
        }
    }
}
