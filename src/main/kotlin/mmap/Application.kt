package mmap

import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import mmap.features.auth.login.configureLoginRouting
import mmap.features.auth.registry.configureRegistryRouting
import mmap.features.catalog.configureCatalogRouting
import mmap.features.maps.configureMapsRouting
import mmap.features.nodes.configureNodesRouting
import mmap.features.testing.configureTestingRouting
import mmap.plugins.*
import org.jetbrains.exposed.sql.Database

fun main() {
    val dotenv = dotenv()
    Database.connect(
        url = dotenv["DATABASE_CONNECTION_STRING"],
        driver = "org.postgresql.Driver",
        user = dotenv["POSTGRES_USER"],
        password = dotenv["POSTGRES_PASSWORD"],
    )
    configureYandex300Api(dotenv["YANDEX_300_API_KEY"])
    configureOpexamsApi(dotenv["OPEXAMS_API_KEY"])
    embeddedServer(
        CIO,
        port = dotenv["SERVER_PORT"].toInt(),
        host = dotenv["SERVER_HOST"],
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureRouting()
    configureFilter()
    configureAuthentication()
    // Custom routes
    configureLoginRouting()
    configureRegistryRouting()
    configureCatalogRouting()
    configureMapsRouting()
    configureNodesRouting()
    configureTestingRouting()
}
