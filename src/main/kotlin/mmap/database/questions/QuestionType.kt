package mmap.database.questions

import kotlinx.serialization.Serializable


@Serializable
enum class QuestionType {
    SINGLE_CHOICE,
    MULTIPLE_CHOICE
}