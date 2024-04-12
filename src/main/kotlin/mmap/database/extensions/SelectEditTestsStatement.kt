package mmap.database.extensions

import mmap.database.nodes.Nodes
import mmap.database.tests.Tests
import mmap.database.tests.TestsDTO
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.booleanParam
import org.jetbrains.exposed.sql.selectAll

fun selectEditTestsStatement(mapId: Int): List<TestsDTO> =
    (Nodes innerJoin Tests)
        .selectAll()
        .where {
            (Nodes.mapId eq mapId) and
                    (Nodes.isRemoved eq booleanParam(false))
        }.map {
            TestsDTO(
                id = it[Tests.id].value,
                nodeId = it[Nodes.id].value,
            )
        }
