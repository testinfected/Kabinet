package com.vtence.kabinet

import java.sql.PreparedStatement

class SelectStatement(
    private val from: FieldSet,
) : Expression, Preparable {

    private var whereClause: Expression? = null
    private var limit: Int? = null
    private var offset: Int = 0
    private var count: Boolean = false

    fun where(clause: Expression): SelectStatement = apply {
        whereClause = clause
    }

    fun limitTo(count: Int, start: Int): SelectStatement = apply {
        limit = count
        offset = start
    }

    fun countOnly(): SelectStatement = apply {
        count = true
    }

    override fun build(statement: SqlStatement) = statement {
        append("SELECT ")
        if (count) {
            append("COUNT(*)")
        } else {
            from.fields.join { +it }
        }
        append(" FROM ")
        +from.source
        whereClause?.let {
            append(" WHERE ")
            +it
        }
        limit?.let { count ->
            append(" LIMIT ").appendValue(count)
            if (offset > 0) append(" OFFSET ").appendValue(offset)
        }
    }

    override fun <T> prepare(query: (PreparedStatement) -> T): JdbcStatement<T> {
        return PreparedJdbcStatement(this, query, false)
    }
}
