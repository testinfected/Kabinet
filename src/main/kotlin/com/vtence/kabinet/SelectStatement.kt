package com.vtence.kabinet

import java.sql.PreparedStatement

class SelectStatement(
    private val from: FieldSet,
) : Expression, Preparable {

    private var whereClause: Expression? = null
    private var limit: Int? = null
    private var offset: Int = 0
    private var count: Boolean = false
    private var distinct: Boolean = false
    private val orderByClauses: MutableList<Expression> = mutableListOf()

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

    fun distinctOnly(): SelectStatement = apply {
        distinct = true
    }

    fun orderBy(expression: Expression) = apply {
        orderByClauses += expression
    }

    override fun build(statement: SqlBuilder) = statement {
        append("SELECT ")
        if (count && distinct) {
            append("COUNT(DISTINCT (")
            from.fields.join { +it }
            append("))")
        } else if (count && !distinct) {
            append("COUNT(*)")
        } else if (distinct) {
            append("DISTINCT ")
            from.fields.join { +it }
        } else {
            from.fields.join { +it }
        }
        append(" FROM ", from.source)
        whereClause?.let {
            append(" WHERE ", it)
        }
        if (orderByClauses.isNotEmpty()) {
            append(" ORDER BY ")
            orderByClauses.join {
                +it
            }
        }

        limit?.let { count ->
            append(" LIMIT ", count)
            if (offset > 0) append(" OFFSET ", offset)
        }
    }

    override fun <T> prepare(query: (PreparedStatement) -> T): JdbcStatement<T> {
        return PreparedJdbcStatement(this, query, false)
    }
}
