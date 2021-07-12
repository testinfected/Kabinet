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


class Select(table: Table, private val columns: List<Column<*>>) : Query() {
    private val statement = SelectStatement(table.tableName, columns.qualifiedNames)
    private val parameters = mutableListOf<Any?>()

    fun where(clause: String, vararg args: Any?): Query = apply {
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
            result += hydrate(ResultRow.readFrom(rs, columns))
        }
        return result.toList()
    }

    override fun toString(): String = statement.toSql()

    companion object {
        fun from(
            table: Table,
            vararg columns: Column<*> = table.columns.toTypedArray(),
        ): Select {
            return Select(table, columns.toList())
        }
    }
}

fun <T : Table> T.select(): Select = Select.from(this, *columns.toTypedArray())

fun <T : Table> T.selectWhere(clause: String, vararg args: Any?): Query = select().where(clause, *args)

fun <T : Table, R> T.selectAll(executor: StatementExecutor, hydrate: ResultRow.() -> R): List<R> =
    select().list(executor, hydrate)

fun <T : Table, R> T.selectAll(connection: Connection, hydrate: ResultRow.() -> R): List<R> =
    selectAll(StatementExecutor(connection), hydrate)

