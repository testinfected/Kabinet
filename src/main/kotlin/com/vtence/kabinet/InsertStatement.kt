package com.vtence.kabinet

import java.sql.PreparedStatement
import java.sql.Statement

class InsertStatement(
    private val tableName: String,
    private val columnNames: List<String>
) : Compilable {

    fun toSql(): String = buildSql {
        append("INSERT INTO ").append(tableName)
        columnNames.join(prefix = "(", postfix = ")") { append(it) }
        append(" VALUES")
        columnNames.join(prefix = "(", postfix = ")") { append("?") }
    }

    override fun <T> compile(parameters: Iterable<Any?>, query: (PreparedStatement) -> T): JdbcStatement<T> {
        return PreparedJdbcStatement(
            toSql(),
            parameters,
            { it.prepareStatement(toSql(), Statement.RETURN_GENERATED_KEYS) },
            query
        )
    }
}
