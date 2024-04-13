package mmap.database.selectedmaps

import mmap.database.extensions.updateAnswerProgressUnmarkStatement
import mmap.database.extensions.updateNodeProgressUnmarkStatement
import mmap.database.maps.Maps
import mmap.database.nodes.Nodes
import mmap.database.tests.Tests
import mmap.database.users.Users
import mmap.database.users.UsersFetchDTO
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object SelectedMaps : Table() {
    val userId = integer("user_id").references(Users.id)
    val mapId = integer("map_id").references(Maps.id)
    val isRemoved = bool("is_removed").default(false)

    override val primaryKey: PrimaryKey = PrimaryKey(userId, mapId)

    fun insert(userId: Int, mapId: Int) = transaction {
        updateStatement(userId, mapId)
    }

    fun selectByUserId(userId: Int): List<SelectedMapDTO> = try {
        transaction {
            selectByUserStatement(userId).map {
                SelectedMapDTO(
                    id = it[SelectedMaps.mapId],
                    title = it[Maps.title],
                    description = it[Maps.description].orEmpty(),
                    admin = UsersFetchDTO(it[Maps.adminId], it[Users.email]),
                    isSaved = true,
                    referralId = it[Maps.referralId],
                    passwordHash = it[Maps.passHash]
                )
            }
        }
    } catch (e: Exception) {
        emptyList()
    }

    fun updateStatement(userId: Int, mapId: Int) =
        upsert(SelectedMaps.mapId, SelectedMaps.userId,
            onUpdate = listOf(isRemoved to booleanParam(false)),
            where = { (SelectedMaps.mapId eq mapId) and (SelectedMaps.userId eq userId) }) {
            it[SelectedMaps.mapId] = mapId
            it[SelectedMaps.userId] = userId
        }

    fun selectByMapIdStatement(mapId: Int): List<UsersFetchDTO> =
        (SelectedMaps innerJoin Users)
            .selectAll()
            .where {
                (SelectedMaps.mapId eq mapId) and
                        isRemoved eq booleanParam(false)
            }.distinct().map {
                UsersFetchDTO(
                    id = it[userId],
                    email = it[Users.email]
                )
            }

    fun getEditableMapIdEditForUserByNodeId(nodeId: UUID, userId: Int): Int? = transaction {
        (SelectedMaps innerJoin Maps innerJoin Nodes)
            .selectAll()
            .where { (Nodes.id eq nodeId) and (SelectedMaps.userId eq userId) and (Maps.adminId eq userId) }
            .firstOrNull()
            ?.let { it[Maps.id].value }
    }

    fun isEnabledInteractForUserByMapId(mapId: Int, userId: Int): Boolean = transaction {
        (SelectedMaps innerJoin Maps)
            .selectAll()
            .where { (SelectedMaps.mapId eq mapId) and (SelectedMaps.userId eq userId) and (Maps.adminId neq userId) }
            .firstOrNull()
            ?.let { !it[isRemoved] } ?: false
    }

    fun isEnabledInteractForUserByNodeId(nodeId: UUID, userId: Int): Boolean = transaction {
        (SelectedMaps innerJoin Maps innerJoin Nodes)
            .selectAll()
            .where { (Nodes.id eq nodeId) and (SelectedMaps.userId eq userId) and (Maps.adminId neq userId) }
            .firstOrNull()
            ?.let { !it[isRemoved] } ?: false
    }

    fun isEnabledInteractForUserByTestId(testId: UUID, userId: Int): Boolean = transaction {
        (SelectedMaps innerJoin Maps innerJoin Nodes innerJoin Tests)
            .selectAll()
            .where { (Tests.id eq testId) and (SelectedMaps.userId eq userId) and (Maps.adminId neq userId) }
            .firstOrNull()
            ?.let { !it[isRemoved] } ?: false
    }

    fun hideMap(userId: Int, mapId: Int) = transaction {
        hideMapStatement(userId, mapId)
    }

    fun deleteInteractedData(userId: Int, mapId: Int) = transaction {
        hideMapStatement(userId, mapId)
        updateNodeProgressUnmarkStatement(userId, mapId)
        updateAnswerProgressUnmarkStatement(userId, mapId)
    }

    fun selectByUserStatement(userId: Int) =
        (SelectedMaps
            .innerJoin(Maps)
            .innerJoin(Users, { Maps.adminId }, { Users.id })
                ).selectAll().where {
                (SelectedMaps.userId eq userId) and
                        (SelectedMaps.isRemoved eq false) and
                        (Maps.isRemoved eq false)
            }

    private fun hideMapStatement(userId: Int, mapId: Int) =
        update(where = { (SelectedMaps.mapId eq mapId) and (SelectedMaps.userId eq userId) }) {
            it[isRemoved] = true
        }
}
