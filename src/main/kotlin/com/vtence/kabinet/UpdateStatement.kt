package com.vtence.kabinet

import java.sql.PreparedStatement

class UpdateStatement(private val table: String, private val columnNames: List<String>) : Compilable {
    private val whereClause = SqlBuilder()

    fun toSql(): String = buildSql {
        append("UPDATE ")
        append(table)
        append(" SET ")
        columnNames.join {
            append(it).append(" = ?")
        }
        if (whereClause.isNotEmpty()) {
            append(" WHERE ").append(whereClause)
        }
    }

    override fun <T> compile(query: (PreparedStatement) -> T): JdbcStatement<T> {
        return JdbcStatement(toSql()) {
            it.prepareStatement(toSql()).use(query)
        }
    }

    fun where(clause: String) {
        whereClause.append(clause)
    }
}
