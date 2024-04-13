package mmap.database.nodeprogress

import mmap.database.nodes.Nodes
import mmap.database.users.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object NodeProgress : Table() {
    val userId = integer("user_id").references(Users.id)
    val nodeId = uuid("node_id").references(Nodes.id)
    val isMarked = bool("is_marked")

    fun toggleNode(nodeId: UUID, userId: Int): Boolean = transaction {
        upsert(
            NodeProgress.userId, NodeProgress.nodeId,
            onUpdate = listOf(isMarked to not(isMarked)),
            where = { (NodeProgress.nodeId eq nodeId) and (NodeProgress.userId eq userId) }) {
            it[NodeProgress.userId] = userId
            it[NodeProgress.nodeId] = nodeId
            it[isMarked] = true
        }
        NodeProgress
            .selectAll()
            .where { (NodeProgress.nodeId eq nodeId) and (NodeProgress.userId eq userId) }
            .single()
            .let { it[isMarked] }
    }
}
