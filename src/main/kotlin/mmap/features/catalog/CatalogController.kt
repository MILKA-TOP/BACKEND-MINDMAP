package mmap.features.catalog

import mmap.domain.catalog.CatalogRepository
import mmap.domain.catalog.models.MapCatalogRemote

class CatalogController(private val catalogRepository: CatalogRepository) {

    fun getAddedDiagrams(userId: Int): List<MapCatalogRemote> {
        return catalogRepository.getUserDiagrams(userId)
    }

    fun searchMaps(userId: Int, query: String): List<MapCatalogRemote> {
        require(query.length >= 3) { "The length og query must be more, than 3" }
        return catalogRepository.getDiagramsByQuery(userId, query)
    }
}
