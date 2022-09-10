package com.vtence.kabinet

import java.sql.Connection


typealias Hydrator<T> = (ResultRow) -> T


abstract class Query<Q : Query<Q>> {

    abstract fun orderBy(vararg expressions: Expression<*>): Q

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

fun <Q : Query<Q>> Q.orderBy(vararg order: Pair<Expression<*>, Sorting>): Q =
    orderBy(*order.map { (expression, sort) -> OrderedBy(expression, sort) }.toTypedArray())

fun <Q : Query<Q>> Q.orderBy(expression: Expression<*>, direction: SortDirection = SortDirection.ASC, option: SortOption? = null): Q =
    orderBy(expression to Sorting(direction, option))

fun <Q : Query<Q>> Q.orderBy(expression: String, vararg parameters: Any?): Q =
    orderBy(expression.asExpression<Nothing>(*parameters))


enum class SortDirection {
    ASC, DESC;

    operator fun invoke(option: SortOption): Sorting = Sorting(this, option)
}

enum class SortOption {
    NULLS_FIRST, NULLS_LAST
}

data class Sorting(val direction: SortDirection, val option: SortOption? = null)

infix fun Expression<*>.to(order: SortDirection) = this to Sorting(order)


private class OrderedBy(private val expression: Expression<*>, private val sorting: Sorting) : Expression<Nothing> {
    override fun build(statement: SqlBuilder) = statement {
        append(expression)
        append(" ", when(sorting.direction) {
            SortDirection.ASC -> "ASC"
            SortDirection.DESC -> "DESC"
        })
        sorting.option?.let {
            append(" ", when(it) {
                SortOption.NULLS_FIRST -> "NULLS FIRST"
                SortOption.NULLS_LAST -> "NULLS LAST"
            })
        }
    }
}

