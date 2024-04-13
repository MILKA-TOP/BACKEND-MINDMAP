package mmap.data.maps

import mmap.database.maps.Maps
import mmap.database.selectedmaps.SelectedMapDTO
import mmap.database.selectedmaps.SelectedMaps
import java.util.*

class MapsDataSource {

    fun isEnabledInteractForUserByNodeId(nodeId: UUID, userId: Int) =
        SelectedMaps.isEnabledInteractForUserByNodeId(nodeId, userId)

    fun selectByUser(userId: Int): List<SelectedMapDTO> = SelectedMaps.selectByUserId(userId)
    fun selectByQuery(userId: Int, query: String): List<SelectedMapDTO> = Maps.selectByQuery(userId, query)
}
