package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import java.sql.Connection

class StatementRecorder(private val connection: Connection): StatementExecutor {
    private val tape: MutableList<String> = mutableListOf()

    val lastStatement: String get() = tape.last()

    override fun <T> execute(statement: JdbcStatement<T>): T {
        return statement.execute(connection).also { tape += statement.toSql(connection) }
    }
}

fun StatementRecorder.assertSql(sql: String) {
    assertThat("generated sql", lastStatement, equalTo(sql))
}
