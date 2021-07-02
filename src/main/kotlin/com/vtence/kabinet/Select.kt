package com.vtence.kabinet

import java.sql.Connection
import java.sql.ResultSet


abstract class Query {

    abstract fun limit(count: Int, offset: Int = 0): Query

    fun <T> first(db: StatementExecutor, hydrate: Hydrator<T>): T? =
        limit(1).list(db, hydrate).firstOrNull()

    abstract fun <T> list(db: StatementExecutor, hydrate: Hydrator<T>): List<T>
}


fun <T> Query.list(connection: Connection, hydrate: Hydrator<T>): List<T> =
    list(StatementExecutor(connection), hydrate)

fun <T> Query.first(connection: Connection, hydrate: Hydrator<T>): T? =
    first(StatementExecutor(connection), hydrate)


class Select(table: Table, columns: List<Column<*>>) : Query() {
    private val statement = SelectStatement(table.tableName, columns.qualifiedNames)
    private val parameters = mutableListOf<Any?>()

    fun where(clause: String, vararg args: Any?): Query = apply {
        statement.where(clause)
        parameters.addAll(args)
    }

    override fun limit(count: Int, offset: Int): Query = apply { statement.limitTo(count, start = offset) }

    override fun <T> list(db: StatementExecutor, hydrate: Hydrator<T>): List<T> {
        return db.execute(statement.compile { select ->
            select.setParameters(parameters)
            read(select.executeQuery(), hydrate)
        })
    }

    private fun <T> read(rs: ResultSet, hydrate: Hydrator<T>): List<T> {
        val result = mutableListOf<T>()
        while (rs.next()) {
            result += hydrate(rs)
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

fun <T : Table> T.selectAll(): Select {
    return Select.from(this, *columns.toTypedArray())
}

fun <T : Table> T.selectWhere(clause: String, vararg args: Any?): Query {
    return selectAll().where(clause, *args)
}
