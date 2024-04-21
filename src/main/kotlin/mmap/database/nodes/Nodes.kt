package mmap.database.nodes

import mmap.database.extensions.UpdateRowDTO
import mmap.database.maps.Maps
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.*
import java.util.*

object Nodes : UUIDTable(columnName = "node_id") {
    val mapId = integer("map_id").references(Maps.id)
    val label = varchar("label", 255)
    val description = text("description").nullable()
    val parentNodeId = uuid("parent_node_id").references(Nodes.id).nullable()
    val isRemoved = bool("is_removed").default(false)
    val priorityNumber = integer("priority_number").default(0)

    fun selectActiveNodesStatement(mapId: Int) =
        selectAll().where { (Nodes.mapId eq mapId) and (isRemoved eq booleanParam(false)) }
            .map {
                NodesDTO(
                    id = it[Nodes.id].value,
                    mapId = it[Nodes.mapId],
                    label = it[Nodes.label],
                    description = it[Nodes.description],
                    parentNodeId = it[Nodes.parentNodeId],
                    priorityPosition = it[priorityNumber]
                )
            }

    fun addStatement(mapId: Int, label: String = "") = insert {
        it[Nodes.mapId] = mapId
        it[Nodes.label] = label
    }

    fun updateRowsStatement(nodes: UpdateRowDTO<NodesDTO, UUID>) {
        batchInsert(nodes.insert) { node ->
            this[id] = node.id
            this[mapId] = node.mapId
            this[label] = node.label
            this[description] = node.description
            this[parentNodeId] = node.parentNodeId
            this[priorityNumber] = node.priorityPosition
        }

        nodes.update.map { node ->
            update(
                where = { Nodes.id eq node.id }
            ) {
                it[label] = node.label
                it[description] = node.description
                it[parentNodeId] = node.parentNodeId
                it[priorityNumber] = node.priorityPosition
            }
        }
        update(
            where = { Nodes.id inList nodes.remove }
        ) {
            it[isRemoved] = true
        }
    }
}
