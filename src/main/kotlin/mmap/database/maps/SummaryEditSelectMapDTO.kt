package mmap.database.maps

import mmap.database.answers.AnswersDTO
import mmap.database.nodes.NodesDTO
import mmap.database.questions.QuestionsDTO
import mmap.database.tests.TestsDTO
import mmap.database.users.UsersFetchDTO

data class SummaryEditSelectMapDTO(
    val id: Int,
    val title: String,
    val description: String,
    val referralId: String,
    val admin: UsersFetchDTO,
    val tests: List<TestsDTO>,
    val nodes: List<NodesDTO>,
    val questions: List<QuestionsDTO>,
    val answers: List<AnswersDTO>,
    val accessUsers: List<UsersFetchDTO>,
)
