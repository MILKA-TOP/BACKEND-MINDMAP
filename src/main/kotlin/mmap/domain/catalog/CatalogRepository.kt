package mmap.domain.catalog

import mmap.data.maps.MapsDataSource
import mmap.database.selectedmaps.SelectedMapDTO
import mmap.domain.catalog.models.MapCatalogRemote
import mmap.domain.maps.models.response.UserResponseRemote.Companion.toDomainModel

class CatalogRepository(
    private val mapsDataSource: MapsDataSource,
) {

    fun getUserDiagrams(userId: Int): List<MapCatalogRemote> {
        val catalogMaps = mapsDataSource.selectByUser(userId)
        return getRemoteCatalogMaps(catalogMaps, userId)
    }

    fun getDiagramsByQuery(userId: Int, query: String): List<MapCatalogRemote> {
        val catalogMaps = mapsDataSource.selectByQuery(userId, query)
        return getRemoteCatalogMaps(catalogMaps, userId)
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
