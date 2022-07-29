package com.vtence.kabinet

import java.sql.PreparedStatement

class UpdateStatement(private val set: ColumnSet, private val values: Iterable<Any?>) : Expression<Nothing>, Preparable {
    private var whereClause: Expression<Boolean>? = null

    fun where(clause: Expression<Boolean>): UpdateStatement = apply {
        whereClause = clause
    }

    override fun build(statement: SqlBuilder) = statement {
        append("UPDATE ", set, " SET ")
        set.columns.zip(values).join { (column, value) ->
            +column.unqualified()
            append(" = ")
            appendArgument(column.type to value)
        }
        whereClause?.let {
            append(" WHERE ")
            +it
        }
    }

    override fun <T> prepare(query: (PreparedStatement) -> T): JdbcStatement<T> {
        return PreparedJdbcStatement(this, query, false)
    }
}
