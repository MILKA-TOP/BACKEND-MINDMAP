package mmap

import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import mmap.di.appModule
import mmap.features.catalog.configureCatalogRouting
import mmap.features.maps.configureMapsRouting
import mmap.features.nodes.configureNodesRouting
import mmap.features.testing.configureTestingRouting
import mmap.features.user.configureUserRouting
import mmap.plugins.*
import org.jetbrains.exposed.sql.Database
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

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
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
    configureHTTP()
    configureSerialization()
    configureRouting()
    configureFilter()
    configureAuthentication()
    // Custom routes
    configureUserRouting()
    configureCatalogRouting()
    configureMapsRouting()
    configureNodesRouting()
    configureTestingRouting()
}
