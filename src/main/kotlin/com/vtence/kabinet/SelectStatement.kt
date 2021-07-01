package com.vtence.kabinet

import java.sql.PreparedStatement

class SelectStatement(
    private val tableName: String,
    private val columnNames: List<String>
) : Compilable {

    private var limit: Int? = null
    private var offset: Int = 0

    fun limitTo(count: Int, start: Int): SelectStatement = apply {
        limit = count
        offset = start
    }

    fun toSql(): String = buildSql {
        append("SELECT ")
        columnNames.join { append(it) }
        append(" FROM ").append(tableName)
        limit?.let { count ->
            append(" LIMIT ").appendValue(count)
            offset.takeUnless { it == 0 }?.let { append(" OFFSET ").appendValue(it) }
        }
    }

    override fun <T> compile(query: (PreparedStatement) -> T): JdbcStatement<T> {
        return JdbcStatement(toSql()) {
            it.prepareStatement(toSql()).use(query)
        }
    }
}
