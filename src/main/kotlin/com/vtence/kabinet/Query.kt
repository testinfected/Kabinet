package com.vtence.kabinet

import java.sql.Connection


typealias Hydrator<T> = (ResultRow) -> T


abstract class Query<Q : Query<Q>> {

    abstract fun orderBy(vararg order: Pair<Expression<*>, SortOrder>): Q

    abstract fun limit(count: Int, offset: Int = 0): Q

    fun <T> firstOrNull(executor: StatementExecutor, hydrate: Hydrator<T>): T? =
        limit(1).list(executor, hydrate).singleOrNull()

    abstract fun <T> list(executor: StatementExecutor, hydrate: Hydrator<T>): List<T>

    abstract fun count(executor: StatementExecutor): Long
}


fun <T, Q : Query<Q>> Q.list(connection: Connection, hydrate: Hydrator<T>): List<T> =
    list(StatementExecutor(connection), hydrate)

fun <T, Q : Query<Q>> Q.count(connection: Connection): Long =
    count(StatementExecutor(connection))

fun <T, Q : Query<Q>> Q.firstOrNull(connection: Connection, hydrate: Hydrator<T>): T? =
    firstOrNull(StatementExecutor(connection), hydrate)


enum class SortOrder {
    ASC, DESC
}

fun <Q : Query<Q>> Q.orderBy(column: Expression<*>, order: SortOrder = SortOrder.ASC): Q = orderBy(column to order)

fun <Q : Query<Q>> Q.orderBy(clause: String, order: SortOrder = SortOrder.ASC, vararg parameters: Any?): Q = orderBy(clause.asExpression<Nothing>(*parameters), order)



