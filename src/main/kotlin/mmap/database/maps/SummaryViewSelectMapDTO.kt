package mmap.database.maps

import mmap.database.answers.AnswersDTO
import mmap.database.nodes.NodesDTO
import mmap.database.questions.QuestionsDTO
import mmap.database.tests.TestsDTO
import mmap.database.users.UsersFetchDTO
import java.util.UUID

data class SummaryViewSelectMapDTO(
    val id: Int,
    val title: String,
    val description: String,
    val referralId: String,
    val admin: UsersFetchDTO,
    val nodes: List<NodesDTO>,
    val tests: List<TestsDTO>,
    val questions: List<QuestionsDTO>,
    val answers: List<AnswersDTO>,
    val selectedAnswersIds: List<UUID>,
    val stampedQuestions: List<QuestionsDTO>,
    val stampedAnswers: List<AnswersDTO>,
    val selectedNodesIds: List<UUID>,
)
