package com.vtence.kabinet

import java.sql.Connection
import java.sql.ResultSet


class Select(private val from: FieldSet) : Query() {
    private val statement = SelectStatement(from)

    fun where(clause: String, vararg args: Any?): Query = where(clause.asExpression(*args))

    fun where(clause: Expression<Boolean>): Query = apply {
        statement.where(clause)
    }

    override fun distinct(): Query = apply {
        statement.distinctOnly()
    }

    override fun orderBy(expression: Expression<Nothing>): Query = apply {
        statement.orderBy(expression)
    }

    override fun limit(count: Int, offset: Int): Query = apply { statement.limitTo(count, start = offset) }

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

    override fun toString(): String = statement.toSql()

    companion object {
        fun from(fields: FieldSet): Select = Select(fields)
    }
}

fun FieldSet.selectAll(): Query = Select.from(this)

fun FieldSet.selectWhere(clause: String, vararg args: Any?): Query =
    selectWhere(clause.asExpression(*args))

fun FieldSet.selectWhere(expression: SqlBuilder.() -> Unit): Query =
    selectWhere(Expression.build(expression))

fun FieldSet.selectWhere(expression: Expression<Boolean>): Query =
    Select.from(this).where(expression)

fun <R> FieldSet.selectAll(executor: StatementExecutor, hydrate: Hydrator<R>): List<R> =
    Select.from(this).list(executor, hydrate)

fun <R> FieldSet.selectAll(connection: Connection, hydrate: Hydrator<R>): List<R> =
    selectAll(StatementExecutor(connection), hydrate)

