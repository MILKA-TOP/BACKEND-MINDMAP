package mmap.database.extensions

import org.jetbrains.exposed.sql.Table
import org.postgresql.util.PGobject

class PGEnum<T : Enum<T>>(enumTypeName: String, enumValue: T?) : PGobject() {
    init {
        value = enumValue?.name
        type = enumTypeName
    }
}

fun <T : Enum<T>> Table.defaultCustomEnumeration(name: String, sql: String, enumClassCast: (Any) -> T) =
    customEnumeration(name = name,
        sql = sql,
        enumClassCast
    ) { PGEnum(sql, it) }

