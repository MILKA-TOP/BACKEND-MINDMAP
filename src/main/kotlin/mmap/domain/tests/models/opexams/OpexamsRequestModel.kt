package mmap.domain.tests.models.opexams

import kotlinx.serialization.Serializable

@Suppress("EnumNaming", "EnumEntryNameCase")
enum class OpexamsRequestType {
    contextBased, topicBased
}

enum class OpexamsQuestionsType {
    MCQ
}

@Serializable
class OpexamsRequestModel(
    val type: OpexamsRequestType,
    val questionType: OpexamsQuestionsType,
    val context: String,
)

@Serializable
data class OpexamsQuestionData(
    val data: List<OpexamsQuestion>
)

@Serializable
data class OpexamsQuestion(
    val id: String,
    val question: String,
    val answer: String,
    val options: List<String>
)
