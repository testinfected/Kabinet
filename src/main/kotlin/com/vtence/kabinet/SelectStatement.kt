package com.vtence.kabinet

import java.sql.PreparedStatement

class SelectStatement(
    private val from: FieldSet,
) : Expression<Nothing>, Preparable {

    private var where: Expression<Boolean>? = null
    private var limit: Int? = null
    private var offset: Int = 0
    private var counting: Boolean = false
    private var distinct: Boolean = false
    private var having: Expression<*>? = null

    private val groupedBy: MutableList<Expression<*>> = mutableListOf()
    private val orderedBy: MutableList<Expression<*>> = mutableListOf()

    fun where(condition: Expression<Boolean>): SelectStatement = apply {
        where = condition
    }

    fun limitTo(count: Int, start: Int): SelectStatement = apply {
        limit = count
        offset = start
    }

    fun countOnly(): SelectStatement = apply {
        counting = true
    }

    fun distinctOnly(): SelectStatement = apply {
        distinct = true
    }

    fun groupBy(vararg columns: Expression<*>): SelectStatement = groupBy(columns.toList())

    fun groupBy(columns: Iterable<Expression<*>>): SelectStatement = apply {
        groupedBy += columns
    }

    fun having(condition: Expression<Boolean>): SelectStatement = apply {
        having = condition
    }

    fun orderBy(vararg expressions: Expression<*>) = orderBy(expressions.toList())

    fun orderBy(expressions: Iterable<Expression<*>>) = apply {
        orderedBy += expressions
    }

    override fun build(statement: SqlBuilder) = statement {
        append("SELECT ")
        if (counting && distinct) {
            append("COUNT(DISTINCT (")
            from.fields.join { +it }
            append("))")
        } else if (counting) {
            append("COUNT(*)")
        } else if (distinct) {
            append("DISTINCT ")
            from.fields.join { +it }
        } else {
            from.fields.join { +it }
        }
        append(" FROM ", from.source)
        where?.let {
            append(" WHERE ", it)
        }

        if(!counting) {
            if (groupedBy.isNotEmpty()) {
                append(" GROUP BY ")
                groupedBy.join { +it }
            }

            having?.let {
                append(" HAVING ", it)
            }

            if (orderedBy.isNotEmpty()) {
                append(" ORDER BY ")
                orderedBy.join { +it }
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
