package com.vtence.kabinet

import com.vtence.kabinet.Expression.Companion.build
import java.sql.Connection
import java.sql.ResultSet


class Select(private val from: FieldSet) : Query<Select>() {
    private val statement = SelectStatement(from)

    fun where(condition: String, vararg args: Any?): Select = where(condition.asExpression(*args))

    fun where(condition: Expression<Boolean>): Select = apply {
        statement.where(condition)
    }

    fun distinct(): Select = apply {
        statement.distinctOnly()
    }

    override fun orderBy(vararg expressions: Expression<*>): Select = apply {
        statement.orderBy(*expressions)
    }

    fun groupBy(vararg columns: Expression<*>): Select = apply {
        statement.groupBy(*columns)
    }

    fun having(condition: Expression<Boolean>): Select = apply {
        statement.having(condition)
    }

    override fun limit(count: Int, offset: Int): Select = apply { statement.limitTo(count, start = offset) }

    override fun <T> list(executor: StatementExecutor, hydrate: Hydrator<T>): List<T> {
        return execute(executor) { rs -> read(rs, hydrate) }
    }

    override fun count(executor: StatementExecutor): Long {
        statement.countOnly()
        return execute(executor) { rs ->
            rs.next()
            rs.getLong(1)
        }
    }

    private fun <T> execute(executor: StatementExecutor, hydrate: (ResultSet) -> T): T {
        return executor.execute(statement.prepare { hydrate(it.executeQuery()) })
    }

    private fun <T> read(rs: ResultSet, items: Hydrator<T>): List<T> {
        val result = mutableListOf<T>()
        while (rs.next()) {
            val row = ResultRow.readFrom(rs, from.fields)
            result += row[items]
        }
        return result.toList()
    }

    override fun <T> asExpression(): Expression<T> = build {
        statement.build(this)
    }

    override fun toString(): String = statement.toSql()

    companion object {
        fun from(fields: FieldSet): Select = Select(fields)
    }
}


fun FieldSet.selectAll(): Select = Select.from(this)

fun FieldSet.selectWhere(clause: String, vararg args: Any?): Select =
    selectWhere(clause.asExpression(*args))

fun FieldSet.selectWhere(expression: SqlBuilder.() -> Unit): Select =
    selectWhere(build(expression))

fun FieldSet.selectWhere(expression: Expression<Boolean>): Select =
    Select.from(this).where(expression)

fun <R> FieldSet.selectAll(executor: StatementExecutor, hydrate: Hydrator<R>): List<R> =
    Select.from(this).list(executor, hydrate)

fun <R> FieldSet.selectAll(connection: Connection, hydrate: Hydrator<R>): List<R> =
    selectAll(StatementExecutor(connection), hydrate)


fun <T> Select.listDistinct(executor: StatementExecutor, hydrate: Hydrator<T>): List<T> =
    distinct().list(executor, hydrate)

fun <T> Select.listDistinct(connection: Connection, hydrate: Hydrator<T>): List<T> =
    listDistinct(StatementExecutor(connection), hydrate)

fun Select.countDistinct(executor: StatementExecutor): Long =
    distinct().count(executor)

fun Select.countDistinct(connection: Connection): Long =
    countDistinct(StatementExecutor(connection))
