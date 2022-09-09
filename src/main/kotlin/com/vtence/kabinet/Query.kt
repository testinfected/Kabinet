package com.vtence.kabinet

import java.sql.Connection


typealias Hydrator<T> = (ResultRow) -> T


abstract class Query {

    abstract fun orderBy(vararg order: Pair<Expression<*>, SortOrder>): Query

    abstract fun limit(count: Int, offset: Int = 0): Query

    fun <T> firstOrNull(executor: StatementExecutor, hydrate: Hydrator<T>): T? =
        limit(1).list(executor, hydrate).singleOrNull()

    abstract fun <T> list(executor: StatementExecutor, hydrate: Hydrator<T>): List<T>

    abstract fun count(executor: StatementExecutor): Long
}


fun <T> Query.list(connection: Connection, hydrate: Hydrator<T>): List<T> =
    list(StatementExecutor(connection), hydrate)

fun Query.count(connection: Connection): Long =
    count(StatementExecutor(connection))

fun <T> Query.firstOrNull(connection: Connection, hydrate: Hydrator<T>): T? =
    firstOrNull(StatementExecutor(connection), hydrate)


enum class SortOrder {
    ASC, DESC
}

fun Query.orderBy(column: Expression<*>, order: SortOrder = SortOrder.ASC): Query = orderBy(column to order)

fun Query.orderBy(clause: String, order: SortOrder = SortOrder.ASC, vararg parameters: Any?): Query = orderBy(clause.asExpression<Nothing>(*parameters), order)



