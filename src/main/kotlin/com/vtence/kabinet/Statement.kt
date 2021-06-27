package com.vtence.kabinet

import java.sql.Connection
import java.sql.PreparedStatement


interface Compilable {
    fun <T> compile(query: (PreparedStatement) -> T): JdbcStatement<T>
}


interface JdbcStatement<T> {
    fun execute(connection: Connection): T

    fun toSql(): String

    companion object {
        operator fun <T> invoke(sql: String, statement: (Connection) -> T): JdbcStatement<T> {
            return object : JdbcStatement<T> {
                override fun execute(connection: Connection): T = statement(connection)

                override fun toSql(): String = sql
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



