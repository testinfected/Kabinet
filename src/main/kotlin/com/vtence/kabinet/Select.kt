package com.vtence.kabinet

import java.sql.Connection


class Select(table: Table, columns: List<Column<*>>) {
    private val statement = SelectStatement(table.tableName, columns.qualifiedNames)

    fun <T> list(db: StatementExecutor, hydrate: Hydrator<T>): List<T> {
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

fun <T> Select.list(connection: Connection, hydrate: Hydrator<T>): List<T> =
    list(StatementExecutor(connection), hydrate)
