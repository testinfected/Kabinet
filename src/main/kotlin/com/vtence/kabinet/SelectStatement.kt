package com.vtence.kabinet

import java.sql.PreparedStatement

class SelectStatement(private val tableName: String, private val columnNames: List<String>) : Compilable {

    fun toSql(): String = buildSql {
        append("SELECT ")
        columnNames.join { append(it) }
        append(" FROM ").append(tableName)
    }

    override fun <T> compile(query: (PreparedStatement) -> T): JdbcStatement<T> {
        return JdbcStatement(toSql()) {
            it.prepareStatement(toSql()).use(query)
        }
    }
}
