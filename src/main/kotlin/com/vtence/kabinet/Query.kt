package com.vtence.kabinet

import java.sql.Connection

abstract class Query {

    abstract fun limit(count: Int, offset: Int = 0): Query

    fun <T> first(executor: StatementExecutor, hydrate: ResultRow.() -> T): T? =
        limit(1).list(executor, hydrate).singleOrNull()

    abstract fun <T> list(executor: StatementExecutor, hydrate: ResultRow.() -> T): List<T>
}


fun <T> Query.list(connection: Connection, hydrate: ResultRow.() -> T): List<T> =
    list(StatementExecutor(connection), hydrate)

fun <T> Query.first(connection: Connection, hydrate: ResultRow.() -> T): T? =
    first(StatementExecutor(connection), hydrate)


