package com.vtence.kabinet

import java.sql.Connection
import java.sql.ResultSet


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


class Select(private val from: ColumnSet) : Query() {
    private val statement = SelectStatement(from)
    private val parameters = mutableListOf<Any?>()

    fun where(clause: String, vararg args: Any?): Query = where(Literal(clause), *args)

    fun where(clause: Expression, vararg args: Any?): Query = apply {
        statement.where(clause)
        parameters.addAll(args)
    }

    override fun limit(count: Int, offset: Int): Query = apply { statement.limitTo(count, start = offset) }

    override fun <T> list(executor: StatementExecutor, hydrate: ResultRow.() -> T): List<T> {
        return executor.execute(statement.compile(parameters) { select ->
            select.setParameters(parameters)
            read(select.executeQuery(), hydrate)
        })
    }

    private fun <T> read(rs: ResultSet, hydrate: ResultRow.() -> T): List<T> {
        val result = mutableListOf<T>()
        while (rs.next()) {
            result += hydrate(ResultRow.readFrom(rs, from.columns))
        }
        return result.toList()
    }

    override fun toString(): String = statement.toSql()

    companion object {
        fun from(columns: ColumnSet): Select = Select(columns)
    }
}

fun <T : ColumnSet> T.select(): Select = Select.from(this)

fun <T : ColumnSet> T.selectAll(): Query = select()

fun <T : ColumnSet> T.selectWhere(clause: String, vararg args: Any?): Query = select().where(clause, *args)

fun <T : ColumnSet, R> T.selectAll(executor: StatementExecutor, hydrate: ResultRow.() -> R): List<R> =
    select().list(executor, hydrate)

fun <T : ColumnSet, R> T.selectAll(connection: Connection, hydrate: ResultRow.() -> R): List<R> =
    selectAll(StatementExecutor(connection), hydrate)

