package com.vtence.kabinet

import java.sql.Connection
import java.sql.ResultSet


class Select(private val from: FieldSet) : Query() {
    private val statement = SelectStatement(from)

    fun where(clause: Expression): Query = apply {
        statement.where(clause)
    }

    override fun distinct(): Select = apply {
        statement.distinctOnly()
    }

    override fun limit(count: Int, offset: Int): Query = apply { statement.limitTo(count, start = offset) }

    override fun <T> list(executor: StatementExecutor, hydrate: ResultRow.() -> T): List<T> {
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

    private fun <T> read(rs: ResultSet, hydrate: ResultRow.() -> T): List<T> {
        val result = mutableListOf<T>()
        while (rs.next()) {
            result += hydrate(ResultRow.readFrom(rs, from.fields))
        }
        return result.toList()
    }

    override fun toString(): String = statement.toSql()

    companion object {
        fun from(fields: FieldSet): Select = Select(fields)
    }
}

fun <T : FieldSet> T.select(): Select = Select.from(this)

fun <T : FieldSet> T.selectAll(): Query = select()

fun <T : FieldSet> T.selectWhere(clause: String, vararg args: Any?): Query =
    selectWhere(clause.asExpression(*args))

fun <T : FieldSet> T.selectWhere(expression: Expression): Query =
    select().where(expression)

fun <T : FieldSet, R> T.selectAll(executor: StatementExecutor, hydrate: ResultRow.() -> R): List<R> =
    select().list(executor, hydrate)

fun <T : FieldSet, R> T.selectAll(connection: Connection, hydrate: ResultRow.() -> R): List<R> =
    selectAll(StatementExecutor(connection), hydrate)

fun <T : FieldSet, R> T.selectFirst(executor: StatementExecutor, hydrate: ResultRow.() -> R): R? =
    select().first(executor, hydrate)

fun <T : FieldSet, R> T.selectFirst(connection: Connection, hydrate: ResultRow.() -> R): R? =
    selectFirst(StatementExecutor(connection), hydrate)

