package mmap.features.maps

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import mmap.core.buildTestResultViewResponse
import mmap.database.extensions.UpdateRowDTO
import mmap.database.maps.CreateMapsDTO
import mmap.database.maps.Maps
import mmap.database.selectedmaps.SelectedMapDTO
import mmap.database.selectedmaps.SelectedMaps
import mmap.extensions.AccessDenied
import mmap.extensions.md5
import mmap.extensions.salt
import mmap.features.maps.models.request.MapRemoveType
import mmap.features.maps.models.request.MapsAddRequestParams
import mmap.features.maps.models.request.MapsCreateRequestParams
import mmap.features.maps.models.request.MapsMigrateRequestParams
import mmap.features.maps.models.response.AnswersEditResponseRemote.Companion.toEditDomainModel
import mmap.features.maps.models.response.AnswersViewResponseRemote.Companion.toViewDomainModel
import mmap.features.maps.models.response.MapActionType
import mmap.features.maps.models.response.MapIdResponseRemote
import mmap.features.maps.models.response.NodesEditResponseRemote.Companion.toEditDomainModel
import mmap.features.maps.models.response.NodesViewResponseRemote.Companion.toViewDomainModel
import mmap.features.maps.models.response.QuestionsEditResponseRemote.Companion.toEditDomainModel
import mmap.features.maps.models.response.QuestionsViewResponseRemote.Companion.toViewDomainModel
import mmap.features.maps.models.response.SummaryEditMapResponseRemote
import mmap.features.maps.models.response.SummaryViewMapResponseRemote
import mmap.features.maps.models.response.TestsEditResponseRemote.Companion.toEditDomainModel
import mmap.features.maps.models.response.TestsViewResponseRemote.Companion.toViewDomainModel
import mmap.features.maps.models.response.UserResponseRemote.Companion.toDomainModel
import java.time.LocalTime
import java.util.*

class MapsController(private val call: ApplicationCall) {

    private val mapsRepository: MapsRepository = MapsRepository()

    suspend fun createNewMap(userId: String) {
        val userIdInt = userId.toInt()
        val mapsCrateParams = call.receive<MapsCreateRequestParams>()

        if (mapsCrateParams.title.trim().isEmpty()) {
            call.respond(HttpStatusCode.InternalServerError, "Incorrect title parameter")
        } else if (mapsCrateParams.password != null && mapsCrateParams.password.trim().length < PASSWORD_MIN_SIZE) {
            call.respond(HttpStatusCode.InternalServerError, "Incorrect password parameter")
        } else {
            val passwordHash = mapsCrateParams.password?.salt()
            val mapIdRef = mapsCrateParams.ref?.toInt()
            val referralId = generateMapsReferralId(userIdInt, mapsCrateParams.title)
            if (mapIdRef == null) {
                val mapId = Maps.insert(
                    CreateMapsDTO(
                        adminId = userIdInt,
                        title = mapsCrateParams.title,
                        description = mapsCrateParams.description,
                        passwordHash = passwordHash,
                        referralId = referralId,
                    )
                )

                call.respond(HttpStatusCode.OK, MapIdResponseRemote(mapId = mapId.toString()))
            } else {
                val checkEnabledMapForUser = SelectedMaps.isEnabledInteractForUserByMapId(mapIdRef, userIdInt)
                require(checkEnabledMapForUser, { "Enabled copy map only for user, who save this map to catalog" })

                val savedMapDTO = Maps.fetchEditSummary(mapIdRef)

                val nodesMapIds = savedMapDTO.nodes.associate { it.id to UUID.randomUUID() }
                val testsMapIds = savedMapDTO.tests.associate { it.id to UUID.randomUUID() }
                val questionsMapIds = savedMapDTO.questions.associate { it.id to UUID.randomUUID() }
                val answersMapIds = savedMapDTO.answers.associate { it.id to UUID.randomUUID() }

                val cratedMapId = Maps.create(
                    createMapsDTO = CreateMapsDTO(
                        adminId = userIdInt,
                        title = mapsCrateParams.title,
                        description = mapsCrateParams.description,
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

                call.respond(HttpStatusCode.OK, MapIdResponseRemote(mapId = cratedMapId.toString()))
            }
        }
    }

    suspend fun migrate(userId: Int, params: MapsMigrateRequestParams) {
        require(params.password == null || params.password != null && params.password.trim().length < PASSWORD_MIN_SIZE,
            { "Incorrect password parameter" })

        val passwordHash = params.password?.salt()

        val mapsDTO = mapsRepository.migrateOtherMindMap(params.text, params.type)
        val referralId = generateMapsReferralId(userId, mapsDTO.title)

        val cratedMapId = Maps.create(
            createMapsDTO = CreateMapsDTO(
                adminId = userId,
                title = mapsDTO.title,
                description = mapsDTO.description,
                passwordHash = passwordHash,
                referralId = referralId
            ),
            nodes = UpdateRowDTO(insert = mapsDTO.nodes),
            tests = UpdateRowDTO(),
            questions = UpdateRowDTO(),
            answers = UpdateRowDTO(),
        )


        call.respond(HttpStatusCode.OK, MapIdResponseRemote(mapId = cratedMapId.toString()))
    }


    suspend fun addNewMap(userId: String) {
        val userIdInt = userId.toInt()
        val mapsAddParams = call.receive<MapsAddRequestParams>()
        val mapIdInt = mapsAddParams.mapId.toInt()

        val map = Maps.select(mapIdInt)

        if (map.passwordHash == null) {
            if (mapsAddParams.password != null) {
                call.respond(HttpStatusCode.InternalServerError, "Incorrect password parameter")
            } else {
                SelectedMaps.insert(userIdInt, mapIdInt)
                call.respond(HttpStatusCode.OK)
            }
        } else {
            val inputPassword = mapsAddParams.password?.salt()
            if (inputPassword != map.passwordHash) {
                call.respond(HttpStatusCode.InternalServerError, "Incorrect password parameter")
            } else {
                SelectedMaps.insert(userIdInt, mapIdInt)
                call.respond(HttpStatusCode.OK)
            }
        }
    }

    suspend fun fetch(requestUserId: String, mapId: String, fetchUserId: String = requestUserId) {
        val requestUserIdIdInt = requestUserId.toInt()
        val fetchUserIdInt = fetchUserId.toInt()
        val mapIdInt = mapId.toInt()

        val selectedMaps = SelectedMaps.selectByUserId(requestUserIdIdInt)
        val map = selectedMaps.firstOrNull { it.id == mapIdInt }
        if (map != null) {
            val domainModel: Any = if (map.admin.id == fetchUserIdInt) {
                fetchEditMap(
                    mapIdInt = mapIdInt,
                    map = map,
                )
            } else {
                fetchViewMap(mapIdInt, fetchUserIdInt, markAsFetchedForUser = fetchUserIdInt == requestUserIdIdInt)
            }
            call.respond(HttpStatusCode.OK, domainModel)
        } else {
            call.respond(AccessDenied, "You doesn't contains this map in your catalog")
        }
    }


    private fun fetchEditMap(
        mapIdInt: Int,
        map: SelectedMapDTO,
    ): SummaryEditMapResponseRemote {
        val dto = Maps.fetchEditSummary(mapIdInt)

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
        val dto = Maps.fetchViewSummary(mapIdInt, userIdInt, markAsFetchedForUser = markAsFetchedForUser)

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

    suspend fun eraseInteractedMaps(mapId: Int, userId: Int, removeType: MapRemoveType) {
        val checkEnabledMapForUser = SelectedMaps.isEnabledInteractForUserByMapId(mapId, userId)

        if (checkEnabledMapForUser) {
            when (removeType) {
                MapRemoveType.HIDE -> SelectedMaps.hideMap(userId, mapId)
                MapRemoveType.DELETE -> SelectedMaps.deleteInteractedData(userId, mapId)
            }
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.Conflict, "You doesn't have access for this node and map")
        }
    }

    suspend fun deleteEditableMap(mapId: Int, userId: Int) {
        val mapAdminId = Maps.selectAdminId(mapId)

        if (mapAdminId == userId) {
            Maps.deleteEditableMap(mapId)
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.Conflict, "You doesn't have access for this node and map")
        }
    }

    private fun generateMapsReferralId(adminId: Int, title: String) =
        "$adminId-$title-${LocalTime.now()}".md5().slice(0 until REFERRAL_ID_SIZE)


    companion object {
        const val PASSWORD_MIN_SIZE = 8
        const val REFERRAL_ID_SIZE = 8
    }

}