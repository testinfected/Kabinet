package com.vtence.kabinet

import java.sql.Connection

abstract class Query {

    abstract fun distinct(): Query

    abstract fun limit(count: Int, offset: Int = 0): Query

    fun <T> first(executor: StatementExecutor, hydrate: ResultRow.() -> T): T? =
        limit(1).list(executor, hydrate).singleOrNull()

    abstract fun <T> list(executor: StatementExecutor, hydrate: ResultRow.() -> T): List<T>

    abstract fun count(executor: StatementExecutor): Long
}


fun <T> Query.list(connection: Connection, hydrate: ResultRow.() -> T): List<T> =
    list(StatementExecutor(connection), hydrate)

fun <T> Query.first(connection: Connection, hydrate: ResultRow.() -> T): T? =
    first(StatementExecutor(connection), hydrate)

fun <T> Query.listDistinct(executor: StatementExecutor, hydrate: ResultRow.() -> T): List<T> =
    distinct().list(executor, hydrate)

fun <T> Query.listDistinct(connection: Connection, hydrate: ResultRow.() -> T): List<T> =
    listDistinct(StatementExecutor(connection), hydrate)



