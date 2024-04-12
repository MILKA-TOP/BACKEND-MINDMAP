package mmap.database.extensions

import mmap.database.mapfetchtime.MapFetchTime
import mmap.database.maps.Maps
import mmap.database.nodes.Nodes
import mmap.database.tests.Tests
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.util.*

fun selectActualFetchIdByTestIdStatement(userId: Int, testId: UUID): UUID = (
        MapFetchTime
            .innerJoin(Maps)
            .innerJoin(Nodes)
            .innerJoin(Tests)
        ).selectAll()
    .where { (MapFetchTime.userId eq userId) and (Tests.id eq testId) }
    .orderBy(MapFetchTime.fetchedAt to SortOrder.DESC)
    .first().let { it[MapFetchTime.id].value }