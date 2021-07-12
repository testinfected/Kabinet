package com.vtence.kabinet

import java.sql.PreparedStatement

class SelectStatement(
    private val tableName: String,
    private val columnNames: List<String>,
) : Compilable {

    private var whereClause: String? = null
    private var limit: Int? = null
    private var offset: Int = 0

    fun where(clause: String): SelectStatement = apply {
        whereClause = clause
    }

    fun limitTo(count: Int, start: Int): SelectStatement = apply {
        limit = count
        offset = start
    }

    fun toSql(): String = buildSql {
        append("SELECT ")
        columnNames.join { append(it) }
        append(" FROM ").append(tableName)
        whereClause?.let { append(" WHERE ").append(it) }
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
