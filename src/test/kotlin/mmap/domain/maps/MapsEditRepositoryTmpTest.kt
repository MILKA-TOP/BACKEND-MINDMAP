package mmap.domain.maps

import io.ktor.server.testing.*
import io.ktor.test.dispatcher.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mmap.data.maps.MapsDataSource
import mmap.database.answers.AnswersDTO
import mmap.database.maps.SummaryEditSelectMapDTO
import mmap.database.nodes.NodesDTO
import mmap.database.questions.QuestionType
import mmap.database.questions.QuestionsDTO
import mmap.database.tests.TestsDTO
import mmap.domain.maps.models.request.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class MapsEditRepositoryTmpTest {

    private lateinit var mapsDataSource: MapsDataSource
    private lateinit var mapsRepository: MapsEditRepository
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")
    private val mapId = 42
    private val sameMapId = mapId

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        mapsDataSource = mockk(relaxed = true)
        mapsRepository = MapsEditRepository(mapsDataSource)
    }

    @Test
    fun successSelectAdminId() {
        val mapId = 1
        val expectedAdminId = 2
        every { mapsDataSource.selectAdminId(mapId) }.returns(expectedAdminId)

        val actualAdminId = mapsRepository.selectAdminId(mapId)

        verify { (mapsDataSource).selectAdminId(mapId) }
        assertEquals(expectedAdminId, actualAdminId)
    }

    @Test
    fun successMapUpdate() = testApplication {
        val requestUserId = 1
        val mapId = 42
        val nodeIdSave = UUID.randomUUID()
        val expectedSummaryMapResponseRemote = mockk<SummaryEditSelectMapDTO>(relaxed = true) {
            every { admin.id } returns requestUserId
            every { id } returns mapId
            every { nodes } returns listOf(mockk<NodesDTO>(relaxed = true) {
                every { id } returns nodeIdSave
                every { parentNodeId } returns null
            })
        }

        every { mapsDataSource.fetchEditSummary(mapId) } returns expectedSummaryMapResponseRemote

        val updateParams = mockk<MapsUpdateRequestParams>(relaxed = true)

        mapsRepository.update(mapId, updateParams)
    }

    @Test
    fun errorByRemoveParentId() = testApplication {
        val requestUserId = 1
        val mapId = 42
        val nodeIdSave = UUID.randomUUID()
        val otherNodeId = UUID.randomUUID()
        val expectedSummaryMapResponseRemote = mockk<SummaryEditSelectMapDTO>(relaxed = true) {
            every { admin.id } returns requestUserId
            every { id } returns mapId
            every { nodes } returns listOf(mockk<NodesDTO>(relaxed = true) {
                every { id } returns nodeIdSave
                every { parentNodeId } returns null
            },
                mockk<NodesDTO>(relaxed = true) {
                    every { id } returns otherNodeId
                    every { parentNodeId } returns nodeIdSave
                })
        }

        every { mapsDataSource.fetchEditSummary(mapId) } returns expectedSummaryMapResponseRemote

        val updateParams = mockk<MapsUpdateRequestParams>(relaxed = true) {
            every { nodes.removed } returns listOf(nodeIdSave.toString())
        }

        assertThrows<IllegalArgumentException>() {
            mapsRepository.update(mapId, updateParams)
        }
    }

    @Test
    fun errorByNoParentId() = testApplication {
        val requestUserId = 1
        val mapId = 42
        val nodeIdSave = UUID.randomUUID()
        val otherNodeId = UUID.randomUUID()
        val expectedSummaryMapResponseRemote = mockk<SummaryEditSelectMapDTO>(relaxed = true) {
            every { admin.id } returns requestUserId
            every { id } returns mapId
            every { nodes } returns listOf(mockk<NodesDTO>(relaxed = true) {
                every { id } returns nodeIdSave
                every { parentNodeId } returns otherNodeId
            },
                mockk<NodesDTO>(relaxed = true) {
                    every { id } returns otherNodeId
                    every { parentNodeId } returns nodeIdSave
                })
        }

        every { mapsDataSource.fetchEditSummary(mapId) } returns expectedSummaryMapResponseRemote

        val updateParams = mockk<MapsUpdateRequestParams>(relaxed = true)

        assertThrows<NoSuchElementException>() {
            mapsRepository.update(mapId, updateParams)
        }
    }

    @Test(IllegalArgumentException::class)
    fun errorRemovedNotInSavedNodeIds() = runTest {
        val incorrectNode = UUID.randomUUID()
        checkRequires {
            every { nodes.removed } returns listOf(incorrectNode.toString())
        }
    }

    @Test(IllegalArgumentException::class)
    fun errorUpdatedNotInSavedNodeIds() = runTest {
        val incorrectNode = UUID.randomUUID()
        checkRequires {
            every { nodes.updated } returns listOf(mockk(relaxed = true) {
                every { nodeId } returns incorrectNode.toString()
            })
        }
    }

    @Test(IllegalArgumentException::class)
    fun errorInsertParentNotInSavedNodeIds() = runTest {
        val incorrectNode = UUID.randomUUID()
        checkRequires {
            every { nodes.insert } returns listOf(mockk(relaxed = true) {
                every { parentNodeId } returns incorrectNode.toString()
            })
        }
    }

    @Test(IllegalArgumentException::class)
    fun errorUpdatedParentNotInSavedNodeIds() = runTest {
        val incorrectNode = UUID.randomUUID()
        val updatedNodeId = UUID.randomUUID()
        checkRequires(nodeIdSave = updatedNodeId) {
            every { nodes.updated } returns listOf(mockk(relaxed = true) {
                every { nodeId } returns updatedNodeId.toString()
                every { parentNodeId } returns incorrectNode.toString()
            })
        }
    }

    @Test(IllegalArgumentException::class)
    fun errorUpdatedAnyMapCorrect() = runTest {
        val incorrectMapId: Int = 43
        val updatedNodeId = UUID.randomUUID()
        checkRequires(nodeIdSave = updatedNodeId) {
            every { nodes.updated } returns listOf(mockk(relaxed = true) {
                every { nodeId } returns updatedNodeId.toString()
                every { mapId } returns incorrectMapId.toString()
                every { parentNodeId } returns updatedNodeId.toString()
            })
        }
    }

    @Test(IllegalArgumentException::class)
    fun errorInsertedAnyMapCorrect() = runTest {
        val incorrectMapId: Int = 43
        val updatedNodeId = UUID.randomUUID()
        checkRequires(nodeIdSave = updatedNodeId) {
            every { nodes.insert } returns listOf(mockk(relaxed = true) {
                every { nodeId } returns updatedNodeId.toString()
                every { mapId } returns incorrectMapId.toString()
                every { parentNodeId } returns updatedNodeId.toString()
            })
        }
    }

    @Test(IllegalArgumentException::class)
    fun errorRemoveTestIsNotEmpty() = runTest {
        val incorrectNode = UUID.randomUUID()
        checkRequires {
            every { tests.removed } returns listOf(incorrectNode.toString())
        }
    }

    @Test(IllegalArgumentException::class)
    fun errorUpdatedTestIsNotEmpty() = runTest {
        checkRequires {
            every { tests.updated } returns listOf(mockk(relaxed = true))
        }
    }

    @Test(IllegalArgumentException::class)
    fun errorInsertTestIsNotEmpty() = runTest {
        checkRequires {
            every { tests.insert } returns listOf(mockk(relaxed = true))
        }
    }

    @Test(IllegalArgumentException::class)
    fun errorQuestionRemovedNotInSaved() = runTest {
        checkRequires {
            every { questions.removed } returns listOf("")
        }
    }

    @Test(IllegalArgumentException::class)
    fun errorQuestionUpdatedNotInSaved() = runTest {
        checkRequires {
            every { questions.updated } returns listOf(mockk(relaxed = true))
        }
    }

    @Test(IllegalArgumentException::class)
    fun errorQuestionInsertNotInSaved() = runTest {
        checkRequires {
            every { questions.insert } returns listOf(mockk(relaxed = true) {
                every { testId } returns UUID.randomUUID().toString()
            })
        }
    }

    @Test(IllegalArgumentException::class)
    fun errorQuestionInsertNotInTestIdMaps() = runTest {
        val questionIdSaved = UUID.randomUUID()
        checkRequires {
            every { questions.insert } returns listOf(mockk(relaxed = true) {
                every { questionId } returns questionIdSaved.toString()
                every { testId } returns UUID.randomUUID().toString()
            })
        }
    }

    @Test(IllegalArgumentException::class)
    fun errorQuestionUpdateNotInTestIdMaps() = runTest {
        val questionIdSaved = UUID.randomUUID()
        checkRequires {
            every { questions.updated } returns listOf(mockk(relaxed = true) {
                every { questionId } returns questionIdSaved.toString()
                every { testId } returns UUID.randomUUID().toString()
            })
        }
    }

    @Test(IllegalArgumentException::class)
    fun errorAnswerRemovedNotInSavedAnswersIds() = runTest {
        checkRequires {
            every { answers.removed } returns listOf("")
        }
    }

    @Test(IllegalArgumentException::class)
    fun errorAnswerUpdatedNotInSavedAnswersIds() = runTest {
        checkRequires {
            every { answers.updated } returns listOf(mockk() {
                every { answerId } returns UUID.randomUUID().toString()
            })
        }
    }

    @Test(IllegalArgumentException::class)
    fun errorInsertQuestionId() = runTest {
        val answerIdSaved = UUID.randomUUID()
        checkRequires(answerIdSave = answerIdSaved) {
            every { answers.insert } returns listOf(mockk() {
                every { answerId } returns answerIdSaved.toString()
                every { questionId } returns UUID.randomUUID().toString()
            })
        }
    }

    @Test(IllegalArgumentException::class)
    fun errorUpdatedQuestionId() = runTest {
        val answerIdSaved = UUID.randomUUID()
        checkRequires(answerIdSave = answerIdSaved) {
            every { answers.updated } returns listOf(mockk() {
                every { answerId } returns answerIdSaved.toString()
                every { questionId } returns UUID.randomUUID().toString()
            })
        }
    }

    @Test(IllegalArgumentException::class)
    fun checkSingleInRowNodes() = runTest {
        val id = UUID.randomUUID()
        val parent = UUID.randomUUID()
        checkSingles(nodeIdSave = parent) {
            every { nodes.insert } returns listOf(mockk(relaxed = true) {
                every { nodeId } returns id.toString()
                every { parentNodeId } returns parent.toString()
                every { mapId } returns sameMapId.toString()
            }, mockk(relaxed = true) {
                every { nodeId } returns id.toString()
                every { parentNodeId } returns parent.toString()
                every { mapId } returns sameMapId.toString()
            }
            )
        }
    }

    @Test
    fun successBigUpdate() = runTest {
        val nodeIdSave = UUID.randomUUID()
        val nodeIdSaveForRemove1 = UUID.randomUUID()
        val nodeIdSaveForUpdate = UUID.randomUUID()
        val nodeIdUpdate = UUID.randomUUID()
        val testIdCreate = UUID.randomUUID()
        val questionCreate = UUID.randomUUID()
        val requestUserId = 1
        val expectedSummaryMapResponseRemote = mockk<SummaryEditSelectMapDTO>(relaxed = true) {
            every { admin.id } returns requestUserId
            every { id } returns mapId
            every { nodes } returns listOf(mockk<NodesDTO>(relaxed = true) {
                every { id } returns nodeIdSave
                every { parentNodeId } returns null
                every { mapId } returns sameMapId
            }, mockk<NodesDTO>(relaxed = true) {
                every { id } returns nodeIdSaveForRemove1
                every { parentNodeId } returns nodeIdSave
                every { mapId } returns sameMapId
            }, mockk<NodesDTO>(relaxed = true) {
                every { id } returns nodeIdSaveForUpdate
                every { parentNodeId } returns nodeIdSave
                every { mapId } returns sameMapId
            })
        }

        every { mapsDataSource.fetchEditSummary(mapId) } returns expectedSummaryMapResponseRemote

        val updateParams = MapsUpdateRequestParams(
            title = "Some title",
            description = "Some description",
            nodes = UpdatedListComponent<NodesUpdateParam, String>(
                insert = listOf(
                    mockk<NodesUpdateParam>(relaxed = true) {
                        every { nodeId } returns UUID.randomUUID().toString()
                        every { parentNodeId } returns nodeIdSave.toString()
                        every { mapId } returns sameMapId.toString()
                    }
                ),
                updated = listOf(
                    mockk<NodesUpdateParam>(relaxed = true) {
                        every { nodeId } returns nodeIdSaveForUpdate.toString()
                        every { parentNodeId } returns nodeIdSave.toString()
                        every { mapId } returns sameMapId.toString()
                    }),
                removed = listOf(nodeIdSaveForRemove1.toString())
            ),
            tests = UpdatedListComponent(
                insert = listOf(
                    mockk<TestUpdateParam>(relaxed = true) {
                        every { nodeId } returns nodeIdSaveForUpdate.toString()
                        every { testId } returns testIdCreate.toString()
                    }
                ),
                updated = emptyList(),
                removed = emptyList()
            ),
            questions = UpdatedListComponent(
                insert = listOf(mockk<QuestionUpdateParam>(relaxed = true) {
                    every { title } returns "Some titel"
                    every { questionId } returns questionCreate.toString()
                    every { testId } returns testIdCreate.toString()
                    every { questionType } returns QuestionType.SINGLE_CHOICE
                })
            ),
            answers = UpdatedListComponent(
                insert = listOf(mockk<AnswerUpdateParam>(relaxed = true) {
                    every { title } returns "Some titel"
                    every { questionId } returns questionCreate.toString()
                    every { answerId } returns UUID.randomUUID().toString()
                    every { isCorrect } returns true
                })
            )
        )
        mapsRepository.update(mapId, updateParams)

        verify(exactly = 1) { mapsDataSource.updateSummaryMap(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun successBigUpdate2() = runTest {
        val nodeIdSave = UUID.randomUUID()
        val nodeIdSaveForRemove1 = UUID.randomUUID()
        val nodeIdSaveForUpdate = UUID.randomUUID()
        val nodeIdUpdate = UUID.randomUUID()
        val testIdCreate = UUID.randomUUID()
        val questionUpdate = UUID.randomUUID()
        val questionUpdate2 = UUID.randomUUID()
        val questionRemoved = UUID.randomUUID()
        val answer1 = UUID.randomUUID()
        val answer2 = UUID.randomUUID()
        val answer3 = UUID.randomUUID()
        val requestUserId = 1
        val expectedSummaryMapResponseRemote = mockk<SummaryEditSelectMapDTO>(relaxed = true) {
            every { admin.id } returns requestUserId
            every { id } returns mapId
            every { nodes } returns listOf(mockk<NodesDTO>(relaxed = true) {
                every { id } returns nodeIdSave
                every { parentNodeId } returns null
                every { mapId } returns sameMapId
            }, mockk<NodesDTO>(relaxed = true) {
                every { id } returns nodeIdSaveForUpdate
                every { parentNodeId } returns nodeIdSave
                every { mapId } returns sameMapId
            })
            every { tests } returns listOf(
                mockk<TestsDTO>(relaxed = true) {
                    every { nodeId } returns nodeIdSaveForUpdate
                    every { id } returns testIdCreate
                }
            )
            every { questions } returns listOf(
                mockk<QuestionsDTO>(relaxed = true) {
                    every { testId } returns testIdCreate
                    every { id } returns questionUpdate
                    every { questionText } returns "Some text"
                    every { type } returns QuestionType.SINGLE_CHOICE
                },
                mockk<QuestionsDTO>(relaxed = true) {
                    every { testId } returns testIdCreate
                    every { id } returns questionUpdate2
                    every { questionText } returns "Some text"
                    every { type } returns QuestionType.MULTIPLE_CHOICE
                },
                mockk<QuestionsDTO>(relaxed = true) {
                    every { testId } returns testIdCreate
                    every { id } returns questionRemoved
                    every { questionText } returns "Some text"
                    every { type } returns QuestionType.SINGLE_CHOICE
                }
            )
            every { answers } returns listOf(
                mockk<AnswersDTO>(relaxed = true) {
                    every { id } returns answer1
                    every { questionId } returns questionUpdate
                    every { isCorrect } returns true
                },
                mockk<AnswersDTO>(relaxed = true) {
                    every { id } returns answer2
                    every { questionId } returns questionUpdate2
                    every { isCorrect } returns true
                },
                mockk<AnswersDTO>(relaxed = true) {
                    every { id } returns answer3
                    every { questionId } returns questionRemoved
                    every { isCorrect } returns true
                }
            )
        }

        every { mapsDataSource.fetchEditSummary(mapId) } returns expectedSummaryMapResponseRemote

        val updateParams = MapsUpdateRequestParams(
            title = "Some title",
            description = "Some description",
            tests = UpdatedListComponent(),
            questions = UpdatedListComponent(
                updated = listOf(mockk<QuestionUpdateParam>(relaxed = true) {
                    every { title } returns "Some titel"
                    every { questionId } returns questionUpdate.toString()
                    every { testId } returns testIdCreate.toString()
                    every { questionType } returns QuestionType.MULTIPLE_CHOICE
                }),
                removed = listOf(questionRemoved.toString())
            ),
            answers = UpdatedListComponent(
                updated = listOf(mockk<AnswerUpdateParam>(relaxed = true) {
                    every { title } returns "Some titel"
                    every { questionId } returns questionUpdate.toString()
                    every { answerId } returns answer1.toString()
                    every { isCorrect } returns true
                }),
                removed = listOf(answer3.toString())
            )
        )
        mapsRepository.update(mapId, updateParams)

        verify(exactly = 1) { mapsDataSource.updateSummaryMap(any(), any(), any(), any(), any(), any(), any()) }
    }

    private suspend fun checkRequires(
        nodeIdSave: UUID = UUID.randomUUID(),
        otherNodeId: UUID = UUID.randomUUID(),
        questionIdSave: UUID = UUID.randomUUID(),
        answerIdSave: UUID = UUID.randomUUID(),
        block: MapsUpdateRequestParams.() -> Unit
    ) {
        val requestUserId = 1
        val expectedSummaryMapResponseRemote = mockk<SummaryEditSelectMapDTO>(relaxed = true) {
            every { admin.id } returns requestUserId
            every { id } returns mapId
            every { nodes } returns listOf(mockk<NodesDTO>(relaxed = true) {
                every { id } returns nodeIdSave
                every { parentNodeId } returns null
            },
                mockk<NodesDTO>(relaxed = true) {
                    every { id } returns otherNodeId
                    every { parentNodeId } returns nodeIdSave
                })
            every { questions } returns listOf(mockk(relaxed = true) {
                every { id } returns questionIdSave
            })

            every { answers } returns listOf(mockk(relaxed = true) {
                every { id } returns answerIdSave
                every { questionId } returns questionIdSave
            })
        }

        every { mapsDataSource.fetchEditSummary(mapId) } returns expectedSummaryMapResponseRemote

        val updateParams = mockk<MapsUpdateRequestParams>(relaxed = true) {
            block()
        }

        mapsRepository.update(mapId, updateParams)
    }

    private suspend fun checkSingles(
        nodeIdSave: UUID = UUID.randomUUID(),
        block: MapsUpdateRequestParams.() -> Unit
    ) {
        val requestUserId = 1
        val expectedSummaryMapResponseRemote = mockk<SummaryEditSelectMapDTO>(relaxed = true) {
            every { admin.id } returns requestUserId
            every { id } returns mapId
            every { nodes } returns listOf(mockk<NodesDTO>(relaxed = true) {
                every { id } returns nodeIdSave
                every { parentNodeId } returns null
                every { mapId } returns sameMapId
            })
        }

        every { mapsDataSource.fetchEditSummary(mapId) } returns expectedSummaryMapResponseRemote

        val updateParams = mockk<MapsUpdateRequestParams>(relaxed = true) {
            block()
        }

        mapsRepository.update(mapId, updateParams)
    }
}
