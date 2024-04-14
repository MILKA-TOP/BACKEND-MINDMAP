package mmap.data.maps

import mmap.database.answerprogress.AnswerProgress
import mmap.database.answerprogress.AnswersProgressDTO
import mmap.database.nodeprogress.NodeProgress
import java.util.*

class NodesDataSource {

    fun toggleNode(nodeId: UUID, userId: Int): Boolean =
        NodeProgress.toggleNode(nodeId, userId)

    fun sendAnswersForTest(answerProgressDTOs: List<AnswersProgressDTO>) = AnswerProgress.addAnswers(answerProgressDTOs)
}
