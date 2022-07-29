package com.vtence.kabinet

import java.sql.PreparedStatement

class DeleteStatement(private val table: Table): Expression<Nothing>, Preparable {
    private var whereClause: Expression<Boolean>? = null

    fun where(clause: Expression<Boolean>): DeleteStatement = apply {
        whereClause = clause
    }

    override fun build(statement: SqlBuilder) = statement {
        append("DELETE FROM ", table)
        whereClause?.let {
            append(" WHERE ", it)
        }
    }

    override fun <T> prepare(query: (PreparedStatement) -> T): JdbcStatement<T> {
        return PreparedJdbcStatement(this, query, false)
    }
}
