package com.vtence.kabinet

import java.sql.Connection


class Update(table: Table, columns: List<Column<*>>, private val values: DataChange) {
    private val statement = UpdateStatement(table.tableName, columns.names)

    fun execute(db: StatementExecutor): Int {
        return db.execute(statement.compile { update ->
            values.applyTo(update)
            update.executeUpdate()
        })
    }

    override fun toString(): String = statement.toSql()

    companion object {
        fun set(
            table: Table,
            vararg columns: Column<*> = table.columns.toTypedArray(),
            values: DataChange
        ): Update {
            return Update(table, columns.toList(), values)
        }
    }
}

fun Update.execute(connection: Connection): Int = execute(StatementExecutor(connection))


fun <T : Table> T.update(record: T.(Dataset) -> Unit): Update {
    val change = Dataset.opened().apply { record(this) }
    return Update.set(this, columns = change.columns.toTypedArray(), values = change)
}
