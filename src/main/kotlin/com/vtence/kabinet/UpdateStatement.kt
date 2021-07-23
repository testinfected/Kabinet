package com.vtence.kabinet

import java.sql.PreparedStatement

class UpdateStatement(private val set: ColumnSet, private val values: Iterable<Any?>) : Expression, Preparable {
    private var whereClause: Expression? = null

    fun where(clause: Expression): UpdateStatement = apply {
        whereClause = clause
    }

    override fun build(statement: SqlStatement) = statement {
        append("UPDATE ")
        +set
        append(" SET ")
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

    override fun <T> prepare(parameters: Iterable<Any?>, query: (PreparedStatement) -> T): JdbcStatement<T> {
        return PreparedJdbcStatement(this, query, false)
    }
}
