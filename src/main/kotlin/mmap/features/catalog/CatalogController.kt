package mmap.features.catalog

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import mmap.database.maps.Maps
import mmap.database.selectedmaps.SelectedMapDTO
import mmap.database.selectedmaps.SelectedMaps
import mmap.features.maps.models.response.UserResponseRemote.Companion.toDomainModel

class CatalogController(private val call: ApplicationCall) {

    suspend fun getAddedDiagrams(userId: String) {
        val userIdInt = userId.toInt()
        val catalogMaps = SelectedMaps.selectByUserId(userIdInt)
        val remoteCatalogMaps = getRemoteCatalogMaps(catalogMaps, userIdInt)
        call.respond(HttpStatusCode.OK, remoteCatalogMaps)
    }

    suspend fun searchMaps(userId: Int, query: String) {
        require(query.length >= 3, { "The length og qeury must be more, than 3" })

        val catalogMaps = Maps.selectByQuery(userId, query)

        val remoteCatalogMaps = getRemoteCatalogMaps(catalogMaps, userId)
        call.respond(HttpStatusCode.OK, remoteCatalogMaps)
    }

    private fun getRemoteCatalogMaps(maps: List<SelectedMapDTO>, userId: Int) = maps.map { map ->
        MapCatalogRemote(
            id = map.id.toString(),
            title = map.title,
            description = map.description,
            isEnableEdit = map.admin.id == userId,
            isSaved = map.isSaved,
            admin = map.admin.toDomainModel(),
            isPrivate = map.passwordHash != null,
        )
    }
}
