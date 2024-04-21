package mmap.data.migrate

import mmap.database.maps.SummaryEditSelectMapDTO
import mmap.database.nodes.NodesDTO
import mmap.database.users.UsersFetchDTO
import mmap.domain.maps.models.request.MigrateType
import java.util.*

class MigrateDataSource {

    fun migrateOtherMindMap(text: String, type: MigrateType): SummaryEditSelectMapDTO = when (type) {
        MigrateType.MINDOMO_TEXT -> importMindomoText(text)
    }

    private fun importMindomoText(diagram: String): SummaryEditSelectMapDTO {
        val nodes = mutableListOf<NodesDTO>()
        val lines = diagram.lines().filter { it.isNotEmpty() }
        val currentMapId = 0
        val defaultAdminDTO = UsersFetchDTO(0, "")
        val defaultReferralId = ""

        fun processNode(
            nodeId: UUID,
            nodeTitle: String,
            description: String?,
            parentNodeId: UUID?,
            priorityPosition: Int
        ) {
            nodes.add(
                NodesDTO(
                    id = nodeId,
                    mapId = currentMapId,
                    label = nodeTitle,
                    description = description,
                    parentNodeId = parentNodeId,
                    priorityPosition = priorityPosition
                )
            )
        }

        fun parseNode(lines: List<String>, startIndex: Int, parentNodeId: UUID?, priorityPosition: Int = 0): Int {
            val nodeId: UUID = UUID.randomUUID()
            val indentLevel = lines[startIndex].startCountWhitespaces()
            val nodeTitle = lines[startIndex].trim()
            var description: String? = null
            var i = startIndex + 1
            var currentChildrenCounter = 0
            while (i < lines.size) {
                val nextIndentLevel = lines[i].startCountWhitespaces()
                if (nextIndentLevel <= indentLevel) {
                    break
                } else if (nextIndentLevel == indentLevel + 2) {
                    // Дочерняя вершина
                    i = parseNode(lines, i, nodeId, priorityPosition = currentChildrenCounter)
                    currentChildrenCounter++
                } else if (nextIndentLevel == indentLevel + 1) {
                    // Описание вершины
                    val newDescriptionLine = lines[i].trim()
                    description = if (description == null) newDescriptionLine else "$description\n$newDescriptionLine"
                    i++
                } else {
                    throw IllegalArgumentException()
                }
            }
            processNode(nodeId, nodeTitle, description, parentNodeId, priorityPosition = priorityPosition)
            return i
        }

        var i = 0
        while (i < lines.size) {
            i = parseNode(lines, i, null)
        }
        val parentNode = nodes.first { it.parentNodeId == null }

        return SummaryEditSelectMapDTO(
            id = currentMapId,
            title = parentNode.label,
            referralId = defaultReferralId,
            description = "",
            admin = defaultAdminDTO,
            tests = emptyList(),
            questions = emptyList(),
            answers = emptyList(),
            accessUsers = emptyList(),
            nodes = nodes.reversed(),
        )
    }

    private fun String.startCountWhitespaces(): Int = this.takeWhile { it.isWhitespace() }.length
}
