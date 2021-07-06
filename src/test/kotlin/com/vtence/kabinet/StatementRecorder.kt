package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import java.sql.Connection

class StatementRecorder(private val executor: StatementExecutor): StatementExecutor {
    constructor(connection: Connection): this(StatementExecutor(connection))

    val executed: MutableList<JdbcStatement<*>> = mutableListOf()

    val lastStatement: JdbcStatement<*> get() = executed.last()

    override fun <T> execute(statement: JdbcStatement<T>): T {
        return executor.execute(statement).also { executed += statement }
    }
}

fun StatementRecorder.assertSql(sql: String) {
    assertThat("generated sql", lastStatement, hasSql(sql))
}
