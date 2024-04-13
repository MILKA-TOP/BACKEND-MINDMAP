package mmap.database.tests

import mmap.database.extensions.UpdateRowDTO
import mmap.database.extensions.selectActualFetchIdByTestIdStatement
import mmap.database.extensions.selectTestsStampedAnswers
import mmap.database.extensions.selectTestsStampedQuestions
import mmap.database.nodes.Nodes
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object Tests : UUIDTable(columnName = "test_id") {
    val nodeId = uuid("node_id").references(Nodes.id)

    fun updateRowsStatement(tests: UpdateRowDTO<TestsDTO, UUID>) {
        batchInsert(tests.insert) {
            this[id] = it.id
            this[nodeId] = it.nodeId
        }
    }

    fun fetchTestByLastActiveStamp(userId: Int, testId: UUID): StampedTestDTO = transaction {
        val actualFetchId = selectActualFetchIdByTestIdStatement(userId, testId)
        val stampedQuestions = selectTestsStampedQuestions(testId, actualFetchId)
        val stampedAnswers = selectTestsStampedAnswers(testId, actualFetchId)

        StampedTestDTO(
            fetchId = actualFetchId,
            stampedQuestions = stampedQuestions,
            stampedAnswers = stampedAnswers,
        )
    }
}
