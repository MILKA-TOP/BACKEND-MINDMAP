package mmap.features.catalog

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import mmap.plugins.authenticateRouting

fun Application.configureCatalogRouting() {

    authenticateRouting {
        get("/catalog") {
            val userId = call.principal<UserIdPrincipal>()?.name!!

            val catalogController = CatalogController(call)
            catalogController.getAddedDiagrams(userId)
        }
    }
    authenticateRouting {
        get("/catalog/search{query}") {
            val userId = call.principal<UserIdPrincipal>()?.name!!.toInt()
            val query = call.parameters["query"]!!

            val catalogController = CatalogController(call)
            catalogController.searchMaps(userId, query)
        }
    }
}