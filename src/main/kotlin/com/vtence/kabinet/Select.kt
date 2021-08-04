package com.vtence.kabinet

import java.sql.Connection
import java.sql.ResultSet


class Select(private val from: FieldSet) : Query() {
    private val statement = SelectStatement(from)

    fun where(clause: String, vararg args: Any?): Query =
        where(PreparedExpression(clause, args.toList()))

    fun where(clause: Expression): Query = apply {
        statement.where(clause)
    }

    override fun limit(count: Int, offset: Int): Query = apply { statement.limitTo(count, start = offset) }

    override fun <T> list(executor: StatementExecutor, hydrate: ResultRow.() -> T): List<T> {
        return executor.execute(statement.prepare { select ->
            read(select.executeQuery(), hydrate)
        })
    }

    private fun <T> read(rs: ResultSet, hydrate: ResultRow.() -> T): List<T> {
        val result = mutableListOf<T>()
        while (rs.next()) {
            result += hydrate(ResultRow.readFrom(rs, from.fields))
        }
        return result.toList()
    }

    override fun toString(): String = statement.asSql()

    companion object {
        fun from(fields: FieldSet): Select = Select(fields)
    }
}

fun <T : FieldSet> T.select(): Select = Select.from(this)

fun <T : FieldSet> T.selectAll(): Query = select()

fun <T : FieldSet> T.selectWhere(clause: String, vararg args: Any?): Query =
    select().where(clause, *args)

fun <T : FieldSet, R> T.selectAll(executor: StatementExecutor, hydrate: ResultRow.() -> R): List<R> =
    select().list(executor, hydrate)

fun <T : FieldSet, R> T.selectAll(connection: Connection, hydrate: ResultRow.() -> R): List<R> =
    selectAll(StatementExecutor(connection), hydrate)

fun <T : FieldSet, R> T.selectFirst(executor: StatementExecutor, hydrate: ResultRow.() -> R): R? =
    select().first(executor, hydrate)

fun <T : FieldSet, R> T.selectFirst(connection: Connection, hydrate: ResultRow.() -> R): R? =
    selectFirst(StatementExecutor(connection), hydrate)

