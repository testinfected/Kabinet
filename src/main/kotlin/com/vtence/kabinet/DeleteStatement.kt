package com.vtence.kabinet

import java.sql.PreparedStatement

class DeleteStatement(private val table: Table): Expression, Preparable {

    override fun build(statement: SqlStatement) = statement {
        append("DELETE FROM ")
        append(table)
    }

    override fun <T> prepare(query: (PreparedStatement) -> T): JdbcStatement<T> {
        return PreparedJdbcStatement(this, query, false)
    }
}
