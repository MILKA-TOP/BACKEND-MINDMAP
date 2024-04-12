package mmap.database.maps

import mmap.database.answers.AnswersDTO
import mmap.database.extensions.*
import mmap.database.mapfetchtime.MapFetchTime
import mmap.database.nodes.Nodes
import mmap.database.nodes.NodesDTO
import mmap.database.questions.QuestionsDTO
import mmap.database.selectedmaps.SelectedMapDTO
import mmap.database.selectedmaps.SelectedMaps
import mmap.database.tests.Tests
import mmap.database.tests.TestsDTO
import mmap.database.users.Users
import mmap.database.users.UsersFetchDTO
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object Maps : IntIdTable(columnName = "map_id") {
    val adminId = integer("admin_id").references(Users.id)
    val passHash = varchar("pass_hash", 64).nullable()
    val title = varchar("title", 255)
    val description = varchar("description", 255).nullable()
    val referralId = varchar("referral_id", 8)
    val isRemoved = bool("is_removed").default(false)

    fun select(mapId: Int): SelectMapDTO = transaction {
        val map = selectAll().where { Maps.id eq mapId }.single()

        SelectMapDTO(
            mapId = map[Maps.id].value,
            title = map[Maps.title],
            description = map[Maps.description].orEmpty(),
            passwordHash = map[Maps.passHash],
            isRemoved = map[Maps.isRemoved],
        )
    }

    fun selectAdminId(mapId: Int): Int? = try {
        transaction {
            select(adminId).where { Maps.id eq mapId }.single()[adminId]
        }
    } catch (e: Exception) {
        null
    }

    fun updateSummaryMap(
        mapId: Int,
        title: String,
        description: String,
        nodes: UpdateRowDTO<NodesDTO, UUID>,
        tests: UpdateRowDTO<TestsDTO, UUID>,
        questions: UpdateRowDTO<QuestionsDTO, UUID>,
        answers: UpdateRowDTO<AnswersDTO, UUID>,
    ) = transaction {
        update({ Maps.id eq mapId }) {
            it[Maps.title] = title
            it[Maps.description] = description
        }

        Nodes.updateRowsStatement(nodes)
        Tests.updateRowsStatement(tests)
        updateQuestionsStatement(questions)
        updateAnswersStatement(answers)
    }


    fun fetchEditSummary(mapId: Int): SummaryEditSelectMapDTO = transaction {
        val map = selectAll().where { Maps.id eq mapId }.single()
        val nodes = Nodes.selectActiveNodesStatement(mapId)
        val tests = selectEditTestsStatement(mapId)
        val questions = selectEditQuestionsStatement(mapId)
        val answers = selectEditAnswersStatement(mapId)
        val accessUsers = SelectedMaps.selectByMapIdStatement(mapId)

        val adminId = map[Maps.adminId]
        val admin = Users.selectByIdStatement(adminId)

        SummaryEditSelectMapDTO(
            id = mapId,
            title = map[title],
            description = map[description].orEmpty(),
            referralId = map[referralId],
            admin = admin,
            tests = tests,
            nodes = nodes,
            questions = questions,
            answers = answers,
            accessUsers = accessUsers,
        )
    }

    fun fetchViewSummary(
        mapId: Int,
        userId: Int,
        markAsFetchedForUser: Boolean = false,
    ): SummaryViewSelectMapDTO = transaction {
        val map = selectAll().where { Maps.id eq mapId }.single()
        val nodes = Nodes.selectActiveNodesStatement(mapId)
        val tests = selectEditTestsStatement(mapId)
        val questions = selectEditQuestionsStatement(mapId)
        val answers = selectEditAnswersStatement(mapId)


        val selectedAnswersIds = selectPickedAnswersIdsStatement(mapId, userId)
        val stampedAnswers = selectMapsStampedAnswersStatement(mapId, userId)
        val stampedQuestions = selectMapsStampedQuestionsStatement(mapId, userId)
        val selectedNodesIds = selectPickedNodesIdsStatement(mapId, userId)

        val adminId = map[Maps.adminId]
        val admin = Users.selectByIdStatement(adminId)

        if (markAsFetchedForUser) MapFetchTime.fetchMapByUserStatement(mapId, userId)

        SummaryViewSelectMapDTO(
            id = mapId,
            title = map[title],
            description = map[description].orEmpty(),
            referralId = map[referralId],
            admin = admin,
            tests = tests,
            nodes = nodes,
            questions = questions,
            answers = answers,
            selectedAnswersIds = selectedAnswersIds,
            stampedQuestions = stampedQuestions,
            stampedAnswers = stampedAnswers,
            selectedNodesIds = selectedNodesIds
        )
    }

    fun insert(createMapsDTO: CreateMapsDTO): Int = transaction {
        val mapId = insert {
            it[adminId] = createMapsDTO.adminId
            it[passHash] = createMapsDTO.passwordHash
            it[title] = createMapsDTO.title
            it[description] = createMapsDTO.description
            it[referralId] = createMapsDTO.referralId
        }.resultedValues?.single()?.get(Maps.id)?.value!!
        Nodes.addStatement(mapId)
        SelectedMaps.updateStatement(userId = createMapsDTO.adminId, mapId = mapId)
        mapId
    }

    fun create(
        createMapsDTO: CreateMapsDTO,
        nodes: UpdateRowDTO<NodesDTO, UUID>,
        tests: UpdateRowDTO<TestsDTO, UUID>,
        questions: UpdateRowDTO<QuestionsDTO, UUID>,
        answers: UpdateRowDTO<AnswersDTO, UUID>,
    ): Int = transaction {
        val mapId = insert {
            it[adminId] = createMapsDTO.adminId
            it[passHash] = createMapsDTO.passwordHash
            it[title] = createMapsDTO.title
            it[description] = createMapsDTO.description
            it[referralId] = createMapsDTO.referralId
        }.resultedValues?.single()?.get(Maps.id)?.value!!
        SelectedMaps.updateStatement(userId = createMapsDTO.adminId, mapId = mapId)

        Nodes.updateRowsStatement(nodes.copy(insert = nodes.insert.map { it.copy(mapId = mapId) }))
        Tests.updateRowsStatement(tests)
        updateQuestionsStatement(questions)
        updateAnswersStatement(answers)

        mapId
    }

    fun deleteEditableMap(mapId: Int) = transaction {
        update(where = { Maps.id eq mapId }) {
            it[isRemoved] = true
        }
    }

    fun selectByQuery(userId: Int, query: String): List<SelectedMapDTO> = transaction {
        val likeQuery = "%${query.lowercase()}%"
        val selectedMapsIds = SelectedMaps.selectByUserStatement(userId).map { it[SelectedMaps.mapId] }
        (Maps.innerJoin(Users, { Maps.adminId }, { Users.id })).selectAll().where {
            (title.lowerCase().like(likeQuery) or description.lowerCase().like(likeQuery) or (referralId.eq(query))) and
                    (isRemoved eq booleanParam(false))
        }.map {
            val mapId = it[Maps.id].value
            SelectedMapDTO(
                id = mapId,
                title = it[title],
                description = it[description].orEmpty(),
                admin = UsersFetchDTO(id = it[adminId], email = it[Users.email]),
                isSaved = mapId in selectedMapsIds,
                referralId = it[referralId],
                passwordHash = it[passHash]
            )
        }
    }
}