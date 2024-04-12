package mmap.database.extensions

import mmap.database.maps.Maps
import mmap.database.nodeprogress.NodeProgress
import mmap.database.nodes.Nodes
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update

fun updateNodeProgressUnmarkStatement(userId: Int, mapId: Int) =
    (Maps innerJoin Nodes innerJoin NodeProgress).update({
        (Maps.id eq mapId) and (NodeProgress.userId eq userId)
    }) {
        it[NodeProgress.isMarked] = false
    }
