package com.vtence.kabinet

import java.sql.Connection


typealias Hydrator<T> = (ResultRow) -> T


abstract class Query {

    abstract fun distinct(): Query

    abstract fun limit(count: Int, offset: Int = 0): Query

    fun <T> firstOrNull(executor: StatementExecutor, hydrate: Hydrator<T>): T? =
        limit(1).list(executor, hydrate).singleOrNull()

    abstract fun <T> list(executor: StatementExecutor, hydrate: Hydrator<T>): List<T>

    abstract fun count(executor: StatementExecutor): Long

    abstract fun orderBy(expression: Expression<Nothing>): Query
}


fun <T> Query.list(connection: Connection, hydrate: Hydrator<T>): List<T> =
    list(StatementExecutor(connection), hydrate)

fun Query.count(connection: Connection): Long =
    count(StatementExecutor(connection))

fun <T> Query.firstOrNull(connection: Connection, hydrate: Hydrator<T>): T? =
    firstOrNull(StatementExecutor(connection), hydrate)

fun <T> Query.listDistinct(executor: StatementExecutor, hydrate: Hydrator<T>): List<T> =
    distinct().list(executor, hydrate)

fun <T> Query.listDistinct(connection: Connection, hydrate: Hydrator<T>): List<T> =
    listDistinct(StatementExecutor(connection), hydrate)

fun Query.countDistinct(executor: StatementExecutor): Long =
    distinct().count(executor)

fun Query.countDistinct(connection: Connection): Long =
    countDistinct(StatementExecutor(connection))


enum class SortOrder {
    ASC, DESC
}

fun Query.orderBy(column: Expression<*>, order: SortOrder = SortOrder.ASC): Query = orderBy(column to order)

fun Query.orderBy(vararg order: Pair<Expression<*>, SortOrder>): Query = apply {
    order.forEach { (clause, sort) ->
        orderBy {
            it.append(clause)
            it.append(" ")
            it.append(sort.name)
        }
    }
}

fun Query.orderBy(clause: String, vararg parameters: Any?): Query =
    orderBy(clause.asExpression(*parameters))



