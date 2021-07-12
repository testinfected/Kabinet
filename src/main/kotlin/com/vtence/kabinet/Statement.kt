package com.vtence.kabinet

import java.sql.Connection
import java.sql.JDBCType
import java.sql.PreparedStatement


interface Compilable {
    fun <T> compile(parameters: Iterable<Any?> = listOf(), query: (PreparedStatement) -> T): JdbcStatement<T>
}


interface JdbcStatement<T> {
    fun execute(connection: Connection): T

    fun toSql(connection: Connection): String
}


class PreparedJdbcStatement<T>(
    private val sql: String,
    private val parameters: Iterable<Any?>,
    private val prepare: (Connection) -> PreparedStatement,
    private val query: (PreparedStatement) -> T
): JdbcStatement<T> {

    override fun execute(connection: Connection): T {
        return prepare(connection).use(query)
    }

    override fun toSql(connection: Connection): String {
        val statement = prepare(connection)
        val metadata = statement.parameterMetaData

        return sql.run {
            val params = parameters.toList()
            var index = 0
            replace("""\?""".toRegex()) {
                val value = params[index++]
                val type = JDBCType.valueOf(metadata.getParameterType(index))
                type.toSql(value)
            }
        }
    }
}


interface StatementExecutor {
    fun <T> execute(statement: JdbcStatement<T>): T

    companion object {
        operator fun invoke(connection: Connection): StatementExecutor {
            return object : StatementExecutor {
                override fun <T> execute(statement: JdbcStatement<T>): T {
                    return statement.execute(connection)
                }
            }
        }
    }
}



