package com.vtence.kabinet

import java.sql.PreparedStatement

class SelectStatement(
    private val from: FieldSet,
) : Compilable {

    private var whereClause: Expression? = null
    private var limit: Int? = null
    private var offset: Int = 0

    fun where(clause: Expression): SelectStatement = apply {
        whereClause = clause
    }

    fun limitTo(count: Int, start: Int): SelectStatement = apply {
        limit = count
        offset = start
    }

    fun toSql(): String = buildSql {
        append("SELECT ")
        from.fields.join { +it }
        append(" FROM ")
        +from.source
        whereClause?.let {
            append(" WHERE ")
            +it
        }
        limit?.let { count ->
            append(" LIMIT ").appendValue(count)
            offset.takeUnless { it == 0 }?.let { append(" OFFSET ").appendValue(it) }
        }
    }

    override fun <T> compile(parameters: Iterable<Any?>, query: (PreparedStatement) -> T): JdbcStatement<T> {
        return PreparedJdbcStatement(toSql(), parameters, { it.prepareStatement(toSql()) }, query)
    }

    override fun toString(): String = toSql()
}
