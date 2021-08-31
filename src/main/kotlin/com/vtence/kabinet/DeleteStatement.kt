package com.vtence.kabinet

import java.sql.PreparedStatement

class DeleteStatement(private val table: Table): Expression, Preparable {
    private var whereClause: Expression? = null

    fun where(clause: Expression): DeleteStatement = apply {
        whereClause = clause
    }

    override fun build(statement: SqlStatement) = statement {
        append("DELETE FROM ")
        append(table)
        whereClause?.let {
            append(" WHERE ")
            +it
        }
    }

    override fun <T> prepare(query: (PreparedStatement) -> T): JdbcStatement<T> {
        return PreparedJdbcStatement(this, query, false)
    }
}
