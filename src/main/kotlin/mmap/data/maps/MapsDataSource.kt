package mmap.data.maps

import mmap.core.IgnoreCoverage
import mmap.database.answers.AnswersDTO
import mmap.database.extensions.UpdateRowDTO
import mmap.database.extensions.selectFetchIdForTest
import mmap.database.maps.CreateMapsDTO
import mmap.database.maps.Maps
import mmap.database.maps.SummaryViewSelectMapDTO
import mmap.database.nodes.NodesDTO
import mmap.database.questions.QuestionsDTO
import mmap.database.selectedmaps.SelectedMapDTO
import mmap.database.selectedmaps.SelectedMaps
import mmap.database.tests.Tests
import mmap.database.tests.TestsDTO
import java.util.*

@IgnoreCoverage
class MapsDataSource {

    fun isEnabledInteractForUserByNodeId(nodeId: UUID, userId: Int) =
        SelectedMaps.isEnabledInteractForUserByNodeId(nodeId, userId)

    fun isEnabledInteractForUserByTestId(testId: UUID, userId: Int) =
        SelectedMaps.isEnabledInteractForUserByNodeId(testId, userId)

    fun isEnabledInteractForUserByMapId(mapId: Int, userId: Int) =
        SelectedMaps.isEnabledInteractForUserByMapId(mapId, userId)

    fun selectMapFetchIdForTest(testId: UUID, userId: Int) = selectFetchIdForTest(testId, userId)

    fun selectByUser(userId: Int): List<SelectedMapDTO> = SelectedMaps.selectByUserId(userId)
    fun selectByQuery(userId: Int, query: String): List<SelectedMapDTO> = Maps.selectByQuery(userId, query)

    fun fetchTestByLastActiveStamp(userId: Int, testId: UUID) = Tests.fetchTestByLastActiveStamp(userId, testId)

    fun fetchEditSummary(mapId: Int) = Maps.fetchEditSummary(mapId)

    fun getEditableMapIdEditForUserByNodeId(nodeId: UUID, userId: Int) =
        SelectedMaps.getEditableMapIdEditForUserByNodeId(nodeId, userId)

    fun updateSummaryMap(
        mapId: Int,
        title: String,
        description: String,
        nodes: UpdateRowDTO<NodesDTO, UUID> = UpdateRowDTO(),
        tests: UpdateRowDTO<TestsDTO, UUID> = UpdateRowDTO(),
        questions: UpdateRowDTO<QuestionsDTO, UUID> = UpdateRowDTO(),
        answers: UpdateRowDTO<AnswersDTO, UUID> = UpdateRowDTO(),
    ) = Maps.updateSummaryMap(
        mapId = mapId,
        title = title,
        description = description,
        nodes = nodes,
        tests = tests,
        questions = questions,
        answers = answers
    )

    fun insertMap(createMapsDTO: CreateMapsDTO): Int = Maps.insert(createMapsDTO)

    fun create(
        createMapsDTO: CreateMapsDTO,
        nodes: UpdateRowDTO<NodesDTO, UUID> = UpdateRowDTO(),
        tests: UpdateRowDTO<TestsDTO, UUID> = UpdateRowDTO(),
        questions: UpdateRowDTO<QuestionsDTO, UUID> = UpdateRowDTO(),
        answers: UpdateRowDTO<AnswersDTO, UUID> = UpdateRowDTO(),
    ): Int = Maps.create(
        createMapsDTO = createMapsDTO,
        nodes = nodes,
        tests = tests,
        questions = questions,
        answers = answers,
    )

    fun insertSelectionNewMap(mapId: Int, userId: Int) {
        SelectedMaps.insert(userId, mapId)
    }

    fun selectPreview(mapId: Int) = Maps.select(mapId)

    fun fetchViewSummary(mapIdInt: Int, userIdInt: Int, markAsFetchedForUser: Boolean): SummaryViewSelectMapDTO =
        Maps.fetchViewSummary(mapIdInt, userIdInt, markAsFetchedForUser)

    fun hideMap(userId: Int, mapId: Int) {
        SelectedMaps.hideMap(userId, mapId)
    }

    fun deleteInteractedData(userId: Int, mapId: Int) {
        SelectedMaps.deleteInteractedData(userId, mapId)
    }

    fun selectAdminId(mapId: Int): Int? = Maps.selectAdminId(mapId)
    fun deleteEditableMap(mapId: Int) {
        Maps.deleteEditableMap(mapId)
    }
}
