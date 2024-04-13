package mmap.data.maps

import mmap.database.nodeprogress.NodeProgress
import java.util.*

class NodesDataSource {

    fun toggleNode(nodeId: UUID, userId: Int): Boolean =
        NodeProgress.toggleNode(nodeId, userId)
}
