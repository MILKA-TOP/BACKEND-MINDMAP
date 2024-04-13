package mmap.data.maps

import mmap.database.selectedmaps.SelectedMaps
import java.util.*

class MapsDataSource {

    fun isEnabledInteractForUserByNodeId(nodeId: UUID, userId: Int) =
        SelectedMaps.isEnabledInteractForUserByNodeId(nodeId, userId)
}
