package mmap.database.extensions

import mmap.database.nodeprogress.NodeProgress
import mmap.database.nodes.Nodes
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.booleanParam
import org.jetbrains.exposed.sql.selectAll
import java.util.*

fun selectPickedNodesIdsStatement(mapId: Int, userId: Int): List<UUID> = (
        NodeProgress innerJoin Nodes)
    .selectAll()
    .where {
        (Nodes.mapId eq mapId) and
                (NodeProgress.userId eq userId) and
                (NodeProgress.isMarked eq booleanParam(true)
                        )
    }.map { it[NodeProgress.nodeId] }