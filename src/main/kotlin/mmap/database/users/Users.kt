package mmap.database.users

import mmap.extensions.citext
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Users : IntIdTable(columnName = "user_id") {
    val email = citext("email").uniqueIndex()
    val passHash = varchar("pass_hash", 64)

    fun insert(userDTO: UserInsertDTO) = transaction {
        Users.insert {
            it[email] = userDTO.email
            it[passHash] = userDTO.password
        }.resultedValues?.single()?.get(Users.id)?.value!!
    }

    fun fetchUser(email: String): UserAuthFetchDTO? {
        return try {
            transaction {
                val userModel = Users.selectAll().where { Users.email.eq(email) }.single()
                UserAuthFetchDTO(
                    id = userModel[Users.id].value,
                    email = userModel[Users.email],
                    password = userModel[Users.passHash],
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    fun selectById(id: Int) = transaction { selectByIdStatement(id) }

    fun selectByIdStatement(id: Int) = selectAll().where { Users.id eq id }.single().let { user ->
        UsersFetchDTO(
            id = user[Users.id].value,
            email = user[email]
        )
    }
}
