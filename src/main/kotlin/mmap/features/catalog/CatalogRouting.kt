package mmap.features.catalog

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mmap.plugins.authenticateRouting
import org.koin.ktor.ext.inject

fun Application.configureCatalogRouting() {

    val catalogController by inject<CatalogController>()

    authenticateRouting {
        get("/catalog") {
            val userId = call.principal<UserIdPrincipal>()?.name!!.toInt()

            val maps = catalogController.getAddedDiagrams(userId)
            call.respond(HttpStatusCode.OK, maps)
        }
    }
    authenticateRouting {
        get("/catalog/search{query}") {
            val userId = call.principal<UserIdPrincipal>()?.name!!.toInt()
            val query = call.parameters["query"]!!

            val maps = catalogController.searchMaps(userId, query)
            call.respond(HttpStatusCode.OK, maps)
        }
    }
}
