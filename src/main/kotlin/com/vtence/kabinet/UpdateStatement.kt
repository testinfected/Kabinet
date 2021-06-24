package com.vtence.kabinet

import java.sql.PreparedStatement

class UpdateStatement(private val table: String, private val columnNames: List<String>): Compilable {
    fun toSql(): String = buildString {
        append("UPDATE ")
        append(table)
        append(" SET ")
        append(columnNames.joinToString { "$it = ?" })
    }

    override fun <T> compile(query: (PreparedStatement) -> T): JdbcStatement<T> {
        return JdbcStatement(toSql()) {
            it.prepareStatement(toSql()).use(query)
        }
    }
}
