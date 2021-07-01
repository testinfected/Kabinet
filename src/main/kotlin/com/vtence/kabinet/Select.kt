package com.vtence.kabinet

import java.sql.Connection


abstract class Query<Q : Query<Q>> {

    abstract fun limit(count: Int, offset: Int = 0): Q

    fun <T> first(db: StatementExecutor, hydrate: Hydrator<T>): T? =
        limit(1).list(db, hydrate).firstOrNull()

    abstract fun <T> list(db: StatementExecutor, hydrate: Hydrator<T>): List<T>
}


fun <T, Q : Query<Q>> Q.list(connection: Connection, hydrate: Hydrator<T>): List<T> =
    list(StatementExecutor(connection), hydrate)

fun <T, Q : Query<Q>> Q.first(connection: Connection, hydrate: Hydrator<T>): T? =
    first(StatementExecutor(connection), hydrate)


class Select(table: Table, columns: List<Column<*>>) : Query<Select>() {
    private val statement = SelectStatement(table.tableName, columns.qualifiedNames)

    override fun limit(count: Int, offset: Int): Select = apply { statement.limitTo(count, start = offset) }

    override fun <T> list(db: StatementExecutor, hydrate: Hydrator<T>): List<T> {
        return db.execute(statement.compile {
            val rs = it.executeQuery()
            val result = mutableListOf<T>()
            while (rs.next()) {
                result += hydrate(rs)
            }
            result.toList()
        })
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
