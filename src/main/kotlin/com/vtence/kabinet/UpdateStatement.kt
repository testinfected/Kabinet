package com.vtence.kabinet

import java.sql.PreparedStatement

class UpdateStatement(private val table: String, private val columnNames: List<String>) : Compilable {
    private val whereClause = SqlBuilder()

    fun where(clause: String) {
        whereClause.append(clause)
    }

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

    override fun <T> compile(parameters: Iterable<Any?>, query: (PreparedStatement) -> T): JdbcStatement<T> {
        return PreparedJdbcStatement(toSql(), parameters, { it.prepareStatement(toSql()) }, query)
    }

    override fun toString(): String = toSql()
}
