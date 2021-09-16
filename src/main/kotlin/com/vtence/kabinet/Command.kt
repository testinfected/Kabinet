package com.vtence.kabinet

import java.sql.Connection

interface Command {
    fun execute(executor: StatementExecutor): Int
}

fun Command.execute(connection: Connection): Int = execute(StatementExecutor(connection))
