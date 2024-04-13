package mmap.database.mapfetchtime

import mmap.database.maps.Maps
import mmap.database.users.Users
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp

object MapFetchTime : UUIDTable(columnName = "fetch_id") {

    val userId = integer("user_id").references(Users.id)
    val mapId = integer("map_id").references(Maps.id)
    val fetchedAt = timestamp("fetched_at")

    fun fetchMapByUserStatement(mapId: Int, userId: Int) {
        insert {
            it[MapFetchTime.userId] = userId
            it[MapFetchTime.mapId] = mapId
        }
    }
}
