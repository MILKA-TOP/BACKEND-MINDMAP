package mmap.domain.maps

import mmap.core.buildTestResultViewResponse
import mmap.data.maps.MapsDataSource
import mmap.data.migrate.MigrateDataSource
import mmap.database.extensions.UpdateRowDTO
import mmap.database.maps.CreateMapsDTO
import mmap.database.maps.Maps
import mmap.database.maps.SelectMapDTO
import mmap.database.selectedmaps.SelectedMapDTO
import mmap.domain.maps.models.request.MapRemoveType
import mmap.domain.maps.models.request.MapsCreateRequestParams
import mmap.domain.maps.models.request.MapsMigrateRequestParams
import mmap.domain.maps.models.response.AnswersEditResponseRemote.Companion.toEditDomainModel
import mmap.domain.maps.models.response.AnswersViewResponseRemote.Companion.toViewDomainModel
import mmap.domain.maps.models.response.MapActionType
import mmap.domain.maps.models.response.NodesEditResponseRemote.Companion.toEditDomainModel
import mmap.domain.maps.models.response.NodesViewResponseRemote.Companion.toViewDomainModel
import mmap.domain.maps.models.response.QuestionsEditResponseRemote.Companion.toEditDomainModel
import mmap.domain.maps.models.response.QuestionsViewResponseRemote.Companion.toViewDomainModel
import mmap.domain.maps.models.response.SummaryEditMapResponseRemote
import mmap.domain.maps.models.response.SummaryMapResponseRemote
import mmap.domain.maps.models.response.SummaryViewMapResponseRemote
import mmap.domain.maps.models.response.TestsEditResponseRemote.Companion.toEditDomainModel
import mmap.domain.maps.models.response.TestsViewResponseRemote.Companion.toViewDomainModel
import mmap.domain.maps.models.response.UserResponseRemote.Companion.toDomainModel
import mmap.extensions.md5
import mmap.extensions.salt
import mmap.features.maps.MapsController
import java.time.LocalTime
import java.util.*

class MapsRepository(
    private val mapsDataSource: MapsDataSource,
    private val migrateDataSource: MigrateDataSource,
) {

    fun isEnabledInteractForUserByMapId(mapId: Int, userId: Int): Boolean =
        mapsDataSource.isEnabledInteractForUserByMapId(mapId, userId)

    fun createNewMap(userId: Int, crateParams: MapsCreateRequestParams): Int {
        require(crateParams.title.trim().isNotEmpty()) { "Incorrect title parameter" }
        val password = crateParams.password
        require(password != null && password.trim().length >= MapsController.PASSWORD_MIN_SIZE || password == null) { "Incorrect password parameter" }

        val passwordHash = crateParams.password?.salt()
        val mapIdRef = crateParams.ref?.toInt()
        val referralId = generateMapsReferralId(userId, crateParams.title)

        val mapId: Int = if (mapIdRef == null) {
            mapsDataSource.insertMap(
                CreateMapsDTO(
                    adminId = userId,
                    title = crateParams.title,
                    description = crateParams.description,
                    passwordHash = passwordHash,
                    referralId = referralId,
                )
            )
        } else {
            val checkEnabledMapForUser = mapsDataSource.isEnabledInteractForUserByMapId(mapIdRef, userId)
            require(checkEnabledMapForUser) { "Enabled copy map only for user, who save this map to catalog" }

            val savedMapDTO = mapsDataSource.fetchEditSummary(mapIdRef)

            val nodesMapIds = savedMapDTO.nodes.associate { it.id to UUID.randomUUID() }
            val testsMapIds = savedMapDTO.tests.associate { it.id to UUID.randomUUID() }
            val questionsMapIds = savedMapDTO.questions.associate { it.id to UUID.randomUUID() }
            val answersMapIds = savedMapDTO.answers.associate { it.id to UUID.randomUUID() }

            mapsDataSource.create(
                createMapsDTO = CreateMapsDTO(
                    adminId = userId,
                    title = crateParams.title,
                    description = crateParams.description,
                    passwordHash = passwordHash,
                    referralId = referralId
                ),
                nodes = UpdateRowDTO(insert = savedMapDTO.nodes.map {
                    it.copy(
                        id = nodesMapIds[it.id]!!,
                        parentNodeId = nodesMapIds[it.parentNodeId],
                    )
                }),
                tests = UpdateRowDTO(insert = savedMapDTO.tests.map {
                    it.copy(
                        id = testsMapIds[it.id]!!,
                        nodeId = nodesMapIds[it.nodeId]!!
                    )
                }),
                questions = UpdateRowDTO(insert = savedMapDTO.questions.map {
                    it.copy(
                        id = questionsMapIds[it.id]!!,
                        testId = testsMapIds[it.testId]!!
                    )
                }),
                answers = UpdateRowDTO(insert = savedMapDTO.answers.map {
                    it.copy(
                        id = answersMapIds[it.id]!!,
                        questionId = questionsMapIds[it.questionId]!!
                    )
                }),
            )
        }
        return mapId
    }

    fun migrate(userId: Int, params: MapsMigrateRequestParams): Int {
        val password = params.password
        require(
            password != null && password.trim().length >= MapsController.PASSWORD_MIN_SIZE || password == null
        ) { "Incorrect password parameter" }

        val passwordHash = password?.salt()

        val mapsDTO = migrateDataSource.migrateOtherMindMap(params.text, params.type)
        val referralId = generateMapsReferralId(userId, mapsDTO.title)

        val cratedMapId = mapsDataSource.create(
            createMapsDTO = CreateMapsDTO(
                adminId = userId,
                title = mapsDTO.title,
                description = mapsDTO.description,
                passwordHash = passwordHash,
                referralId = referralId
            ),
            nodes = UpdateRowDTO(insert = mapsDTO.nodes),
        )
        return cratedMapId
    }

    fun insertSelectionNewMap(mapId: Int, userId: Int) = mapsDataSource.insertSelectionNewMap(mapId, userId)
    private fun generateMapsReferralId(adminId: Int, title: String) =
        "$adminId-$title-${LocalTime.now()}".md5().slice(0 until MapsController.REFERRAL_ID_SIZE)

    fun selectMapPreview(mapId: Int): SelectMapDTO = mapsDataSource.selectPreview(mapId)
    fun fetch(requestUserId: Int, mapId: Int, fetchUserId: Int): SummaryMapResponseRemote? {
        val selectedMaps = mapsDataSource.selectByUser(requestUserId)
        val map = selectedMaps.firstOrNull { it.id == mapId }
        return map?.let { map ->
            val domainModel: SummaryMapResponseRemote = if (map.admin.id == fetchUserId) {
                fetchEditMap(
                    mapIdInt = mapId,
                    map = map,
                )
            } else {
                fetchViewMap(mapId, fetchUserId, markAsFetchedForUser = fetchUserId == requestUserId)
            }
            return domainModel
        }
    }

    private fun fetchEditMap(
        mapIdInt: Int,
        map: SelectedMapDTO,
    ): SummaryEditMapResponseRemote {
        val dto = mapsDataSource.fetchEditSummary(mapIdInt)

        val answersModels = dto.answers.map { it.toEditDomainModel() }.groupBy { it.questionId }
        val questionsModels =
            dto.questions.map {
                it.toEditDomainModel(answersModels[it.id.toString()]!!)
            }.groupBy { it.testId }
        val testModels = dto.tests.map {
            it.toEditDomainModel(questionsModels[it.id.toString()]!!)
        }.groupBy { it.nodeId }

        return SummaryEditMapResponseRemote(
            id = dto.id.toString(),
            title = dto.title,
            description = dto.description,
            referralId = dto.referralId,
            admin = dto.admin.toDomainModel(),
            nodes = dto.nodes.map {
                it.toEditDomainModel(
                    test = testModels[it.id.toString()]?.firstOrNull()
                )
            },
            accessUsers = dto.accessUsers.filter { it.id != map.admin.id }.map { it.toDomainModel() },
        )
    }

    private fun fetchViewMap(
        mapIdInt: Int,
        userIdInt: Int,
        markAsFetchedForUser: Boolean
    ): SummaryViewMapResponseRemote {
        val dto = mapsDataSource.fetchViewSummary(mapIdInt, userIdInt, markAsFetchedForUser = markAsFetchedForUser)

        val selectedAnswers = dto.selectedAnswersIds
        val markedNodesIds = dto.selectedNodesIds
        val baseAnswersModels = dto.answers.map { it.toViewDomainModel() }.groupBy { it.questionId }
        val questionsModels =
            dto.questions.map {
                it.toViewDomainModel(baseAnswersModels[it.id.toString()]!!)
            }.groupBy { it.testId }

        val summaryTestModels = buildTestResultViewResponse(
            selectedAnswers = selectedAnswers,
            stampedAnswers = dto.stampedAnswers,
            stampedQuestions = dto.stampedQuestions,
            testsIds = dto.tests.map { it.id },
        )

        val testModels = dto.tests.map { test ->
            val resultModel = summaryTestModels[test.id]
            test.toViewDomainModel(
                questionsModels[test.id.toString()]!!,
                testResult = resultModel
            )
        }.groupBy { it.nodeId }

        return SummaryViewMapResponseRemote(
            id = dto.id.toString(),
            title = dto.title,
            description = dto.description,
            referralId = dto.referralId,
            admin = dto.admin.toDomainModel(),
            nodes = dto.nodes.map {
                it.toViewDomainModel(
                    isSelected = it.id in markedNodesIds,
                    test = testModels[it.id.toString()]?.firstOrNull()
                )
            },
            type = MapActionType.INTERACT,
        )
    }

    fun eraseInteractedMaps(mapId: Int, userId: Int, removeType: MapRemoveType) {
        when (removeType) {
            MapRemoveType.HIDE -> mapsDataSource.hideMap(userId, mapId)
            MapRemoveType.DELETE -> mapsDataSource.deleteInteractedData(userId, mapId)
        }
    }

    fun selectAdminId(mapId: Int): Int? = mapsDataSource.selectAdminId(mapId)
    fun deleteEditableMap(mapId: Int) {
        mapsDataSource.deleteEditableMap(mapId)
    }
}
